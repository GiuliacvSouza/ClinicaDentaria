package bll;

import dal.ProcedimentoRepository;
import model.Procedimento;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class ProcedimentoService {
    private static final Set<String> TIPOS_VALIDOS = Set.of("terapeutico", "protese", "estetico");
    private static final Set<BigDecimal> TAXAS_IVA_VALIDAS = Set.of(
            BigDecimal.ZERO,
            new BigDecimal("6"),
            new BigDecimal("23")
    );

    private final ProcedimentoRepository repository;

    public ProcedimentoService(ProcedimentoRepository repository) {
        this.repository = repository;
    }

    public Procedimento salvar(Procedimento procedimento) {

        if (procedimento.getNome() == null || procedimento.getNome().isBlank()) {
            throw new RuntimeException("Nome do procedimento é obrigatório");
        }

        if (procedimento.getValor() != null &&
                procedimento.getValor().doubleValue() < 0) {

            throw new RuntimeException("Valor do procedimento inválido");
        }

        if (procedimento.getDuracaoEstimada() != null &&
                procedimento.getDuracaoEstimada() < 0) {

            throw new RuntimeException("Duração estimada inválida");
        }

        if (procedimento.getTipo() == null || procedimento.getTipo().isBlank()) {
            throw new RuntimeException("Tipo do procedimento é obrigatório");
        }

        String tipoNormalizado = procedimento.getTipo().trim().toLowerCase();
        if (!TIPOS_VALIDOS.contains(tipoNormalizado)) {
            throw new RuntimeException("Tipo de procedimento inválido. Use terapeutico, protese ou estetico.");
        }
        procedimento.setTipo(tipoNormalizado);

        BigDecimal taxaIva = procedimento.getTaxaIva() != null ? procedimento.getTaxaIva() : BigDecimal.ZERO;
        if (!TAXAS_IVA_VALIDAS.contains(taxaIva.stripTrailingZeros())) {
            throw new RuntimeException("Taxa de IVA inválida. Use 0, 6 ou 23.");
        }
        procedimento.setTaxaIva(taxaIva);

        return repository.save(procedimento);
    }

    public List<Procedimento> listarTodos() {
        return repository.findAll();
    }

    public Procedimento buscarPorId(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Procedimento não encontrado."));
    }
}
