package bll;

import dal.AtendimentoRepository;
import dal.FaturaRepository;
import model.Atendimento;
import model.AtendimentoProcedimento;
import model.Fatura;
import model.Procedimento;
import model.enums.EstadoFatura;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FaturaService {

    private final FaturaRepository repository;
    private final AtendimentoRepository atendimentoRepository;

    public FaturaService(FaturaRepository repository, AtendimentoRepository atendimentoRepository) {
        this.repository = repository;
        this.atendimentoRepository = atendimentoRepository;
    }

    private record ResumoFatura(BigDecimal valorBase, BigDecimal taxaIva) {
    }

    private Atendimento carregarAtendimento(Integer atendimentoId) {
        return atendimentoRepository.findById(atendimentoId)
                .orElseThrow(() -> new RuntimeException("Atendimento não encontrado."));
    }

    private List<AtendimentoProcedimento> obterProcedimentos(Atendimento atendimento) {
        List<AtendimentoProcedimento> procedimentos = atendimento.getProcedimentos();
        if (procedimentos == null || procedimentos.isEmpty()) {
            throw new RuntimeException("Atendimento sem procedimentos associados.");
        }
        return procedimentos;
    }

    private ResumoFatura calcularResumo(List<AtendimentoProcedimento> procedimentos) {
        BigDecimal valorBaseTotal = BigDecimal.ZERO;
        BigDecimal taxaIvaComum = null;

        for (AtendimentoProcedimento ap : procedimentos) {
            Procedimento procedimento = ap.getProcedimento();
            if (procedimento == null || procedimento.getValor() == null) {
                throw new RuntimeException("Procedimento inválido para emissão de fatura.");
            }

            int quantidade = ap.getQuantidade() != null ? ap.getQuantidade() : 1;
            BigDecimal descontoPercent = ap.getDesconto() != null ? ap.getDesconto() : BigDecimal.ZERO;
            BigDecimal taxaIvaItem = procedimento.getTaxaIva() != null ? procedimento.getTaxaIva() : BigDecimal.ZERO;

            if (taxaIvaComum == null) {
                taxaIvaComum = taxaIvaItem;
            } else if (taxaIvaComum.compareTo(taxaIvaItem) != 0) {
                throw new RuntimeException("Não é possível emitir fatura com procedimentos de taxas de IVA diferentes.");
            }

            BigDecimal subtotal = procedimento.getValor().multiply(BigDecimal.valueOf(quantidade));
            BigDecimal valorBaseItem = subtotal.multiply(
                    BigDecimal.ONE.subtract(descontoPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
            );

            valorBaseTotal = valorBaseTotal.add(valorBaseItem);
        }

        return new ResumoFatura(
                valorBaseTotal.setScale(2, RoundingMode.HALF_UP),
                taxaIvaComum != null ? taxaIvaComum : BigDecimal.ZERO
        );
    }

    private Fatura criarFatura(Atendimento atendimento, List<AtendimentoProcedimento> procedimentos) {
        ResumoFatura resumo = calcularResumo(procedimentos);

        Fatura fatura = new Fatura();
        fatura.setAtendimento(atendimento);
        fatura.setDataEmissao(LocalDate.now());
        fatura.setEstado(EstadoFatura.PENDENTE);
        fatura.setValorBase(resumo.valorBase());
        fatura.setTaxaIva(resumo.taxaIva());

        return repository.save(fatura);
    }

    @Transactional
    public Fatura emitirFatura(Integer atendimentoId, BigDecimal taxaIvaFallback) {
        Atendimento atendimento = carregarAtendimento(atendimentoId);
        List<AtendimentoProcedimento> procedimentos = obterProcedimentos(atendimento);
        return criarFatura(atendimento, procedimentos);
    }

    @Transactional
    public Fatura emitirFaturaPorAtendimento(Atendimento atendimento) {
        if (atendimento == null) {
            throw new RuntimeException("Atendimento não informado.");
        }

        Atendimento atendimentoGerido = atendimento;
        if (atendimento.getId() != null) {
            atendimentoGerido = carregarAtendimento(atendimento.getId());
        }

        List<AtendimentoProcedimento> procedimentos = obterProcedimentos(atendimentoGerido);
        return criarFatura(atendimentoGerido, procedimentos);
    }

    public List<Fatura> listarTodos() {
        return repository.findAll();
    }

    public Fatura buscarPorId(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fatura não encontrada."));
    }

    public void excluir(Integer id) {
        repository.deleteById(id);
    }

    public Fatura salvar(Fatura fatura) {
        if (fatura.getValorBase() == null || fatura.getValorBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor base da fatura é obrigatório e deve ser positivo");
        }

        BigDecimal taxa = fatura.getTaxaIva();
        if (taxa != null && !taxa.equals(BigDecimal.ZERO)
                && !taxa.equals(new BigDecimal("6"))
                && !taxa.equals(new BigDecimal("23"))) {
            throw new RuntimeException("Taxa de IVA inválida. Use 0, 6 ou 23");
        }

        if (taxa == null) {
            fatura.setTaxaIva(BigDecimal.ZERO);
        }

        if (fatura.getDataEmissao() == null) {
            fatura.setDataEmissao(LocalDate.now());
        }

        if (fatura.getEstado() == null) {
            fatura.setEstado(EstadoFatura.PENDENTE);
        }

        return repository.save(fatura);
    }

}
