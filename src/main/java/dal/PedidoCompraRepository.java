package dal;

import model.PedidoCompra;
import model.enums.EstadoPedidoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Integer> {

    List<PedidoCompra> findByIdFornecedor_Id(Integer idFornecedor);
    List<PedidoCompra> findByIdAssistente_Id(Integer idAssistente);
    List<PedidoCompra> findByEstado(EstadoPedidoCompra estado);

    @Query("SELECT p FROM PedidoCompra p ORDER BY p.dataPedido DESC NULLS LAST")
    List<PedidoCompra> findAllOrderByDataDesc();
}
