package dal;
import model.Pagamento;
import model.enums.MetodoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {
    List<Pagamento> findByIdFatura_Id(Integer idFatura);
    List<Pagamento> findByIdUtilizador_Id(Integer idUtilizador);
    List<Pagamento> findByMetodo(MetodoPagamento metodo);
}