package bll;

import dal.PagamentoRepository;
import model.Pagamento;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PagamentoService {

    private final PagamentoRepository repository;

    public PagamentoService(PagamentoRepository repository) {
        this.repository = repository;
    }

    public Pagamento registrarPagamento(Pagamento pagamento) {

        if (pagamento.getIdFatura() == null) {
            throw new RuntimeException("Pagamento deve estar associado a uma fatura.");
        }

        if (pagamento.getValorPago() == null ||
                pagamento.getValorPago().doubleValue() <= 0) {

            throw new RuntimeException("Valor pago inválido.");
        }

        if (pagamento.getDataPagamento() != null &&
                pagamento.getDataPagamento().isAfter(LocalDate.now())) {

            throw new RuntimeException("Data de pagamento não pode ser futura.");
        }

        return repository.save(pagamento);
    }

    public List<Pagamento> listarTodos() {
        return repository.findAll();
    }

    public Pagamento buscarPorId(Integer id) {

        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado."));
    }

    public List<Pagamento> listarPorFatura(Integer faturaId) {
        return repository.findByIdFatura_Id(faturaId);
    }
}