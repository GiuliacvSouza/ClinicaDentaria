package dal;
import model.PedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Integer> {
    List<PedidoCompra> findByIdFornecedor_Id(Integer idFornecedor);
    List<PedidoCompra> findByIdAssistente_Id(Integer idAssistente);
}