package bll;

import dal.AssistenteRepository;
import dal.MaterialRepository;
import dal.MovimentacaoEstoqueRepository;
import model.Material;
import model.MovimentacaoEstoque;
import model.enums.TipoMovimentacao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MovimentacaoEstoqueService {

    private final MovimentacaoEstoqueRepository repository;
    private final MaterialRepository            materialRepository;
    private final AssistenteRepository          assistenteRepository;

    public MovimentacaoEstoqueService(MovimentacaoEstoqueRepository repository,
                                      MaterialRepository materialRepository,
                                      AssistenteRepository assistenteRepository) {
        this.repository          = repository;
        this.materialRepository  = materialRepository;
        this.assistenteRepository = assistenteRepository;
    }

    // ─── Listagem ─────────────────────────────────────────────────────────────

    public List<MovimentacaoEstoque> listarTodos() {
        return repository.findAllByOrderByDataDesc();
    }

    public List<MovimentacaoEstoque> listarPorMaterial(String nome) {
        if (nome == null || nome.isBlank()) return listarTodos();
        return repository.findByIdMaterial_NomeContainingIgnoreCaseOrderByDataDesc(nome.trim());
    }

    public List<MovimentacaoEstoque> listarPorPeriodo(LocalDate inicio, LocalDate fim) {
        return repository.findByDataBetweenOrderByDataDesc(inicio, fim);
    }

    // ─── Registo — método principal ───────────────────────────────────────────

    /**
     * Regista uma movimentação de stock (entrada ou saída).
     * Actualiza a quantidade do material e persiste o registo.
     */
    @Transactional
    public MovimentacaoEstoque registarMovimentacao(Integer materialId,
                                                    Integer assistenteId,
                                                    TipoMovimentacao tipo,
                                                    Integer quantidade,
                                                    String motivo,
                                                    String observacao) {
        if (tipo == null)
            throw new RuntimeException("O tipo de movimentação é obrigatório.");

        Material material = buscarMaterialAtivo(materialId);
        validarQuantidade(quantidade);
        validarMotivo(motivo);

        int atual = material.getQuantidadeAtual() != null ? material.getQuantidadeAtual() : 0;

        if (tipo == TipoMovimentacao.SAIDA) {
            if (atual < quantidade)
                throw new RuntimeException(
                        "A quantidade indicada é superior ao stock disponível. Stock atual: " + atual);
            material.setQuantidadeAtual(atual - quantidade);
        } else {
            material.setQuantidadeAtual(atual + quantidade);
        }

        materialRepository.save(material);

        MovimentacaoEstoque mov = new MovimentacaoEstoque();
        mov.setIdMaterial(material);
        mov.setTipoMovimentacao(tipo);
        mov.setQuantidade(quantidade);   // sempre positivo
        mov.setData(LocalDate.now());
        mov.setMotivo(motivo.trim());
        if (observacao != null && !observacao.isBlank())
            mov.setObservacao(observacao.trim());

        if (assistenteId != null)
            assistenteRepository.findById(assistenteId).ifPresent(mov::setIdUtilizador);

        return repository.save(mov);
    }

    // ─── Conveniência ─────────────────────────────────────────────────────────

    @Transactional
    public MovimentacaoEstoque registarEntrada(Integer materialId, Integer assistenteId,
                                               Integer quantidade, String motivo, String observacao) {
        return registarMovimentacao(materialId, assistenteId,
                TipoMovimentacao.ENTRADA, quantidade, motivo, observacao);
    }

    @Transactional
    public MovimentacaoEstoque registarSaida(Integer materialId, Integer assistenteId,
                                             Integer quantidade, String motivo, String observacao) {
        return registarMovimentacao(materialId, assistenteId,
                TipoMovimentacao.SAIDA, quantidade, motivo, observacao);
    }

    // ─── Utilitários internos ─────────────────────────────────────────────────

    private Material buscarMaterialAtivo(Integer materialId) {
        Material m = materialRepository.findById(materialId)
                .orElseThrow(() -> new RuntimeException("Material não encontrado."));
        if (Boolean.FALSE.equals(m.getAtivo()))
            throw new RuntimeException("Não é possível movimentar um material inativo.");
        return m;
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0)
            throw new RuntimeException("A quantidade deve ser superior a zero.");
    }

    private void validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank())
            throw new RuntimeException("Indique o motivo da movimentação.");
    }

    // ─── Helpers estáticos para a camada de apresentação ─────────────────────

    /** Texto legível do tipo para mostrar na tabela/badge. */
    public static String textoTipo(TipoMovimentacao tipo) {
        if (tipo == null) return "Movimentação";
        return switch (tipo) {
            case ENTRADA -> "Entrada";
            case SAIDA   -> "Saída";
        };
    }

    /** Classe CSS do badge de tipo. */
    public static String classeTipoBadge(TipoMovimentacao tipo) {
        if (tipo == null) return "badge-tipo-outro";
        return switch (tipo) {
            case ENTRADA -> "badge-tipo-entrada";
            case SAIDA   -> "badge-tipo-saida";
        };
    }

    /**
     * Extrai o tipo de uma movimentação.
     * Usa tipoMovimentacao se existir; fallback pelo prefixo do motivo para dados antigos.
     */
    public static TipoMovimentacao extrairTipoEnum(MovimentacaoEstoque m) {
        if (m == null) return null;
        if (m.getTipoMovimentacao() != null) return m.getTipoMovimentacao();
        // fallback para registos anteriores à migração
        String motivo = m.getMotivo();
        if (motivo != null) {
            if (motivo.startsWith("ENTRADA")) return TipoMovimentacao.ENTRADA;
            if (motivo.startsWith("SAÍDA") || motivo.startsWith("SAIDA"))
                return TipoMovimentacao.SAIDA;
        }
        if (m.getQuantidade() != null && m.getQuantidade() > 0) return TipoMovimentacao.ENTRADA;
        if (m.getQuantidade() != null && m.getQuantidade() < 0) return TipoMovimentacao.SAIDA;
        return null;
    }
}
