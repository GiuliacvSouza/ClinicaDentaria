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

    // JOIN FETCH do fornecedor e assistente para evitar LazyInitializationException no FX thread.
    // Os itens NÃO são carregados aqui (evita "multiple bags fetch" do Hibernate).
    // O PedidoCompra.getItens() é acedido via a colecção já mapeada no JPA context.
    @Query("SELECT DISTINCT p FROM PedidoCompra p " +
           "LEFT JOIN FETCH p.idFornecedor " +
           "LEFT JOIN FETCH p.idAssistente a " +
           "LEFT JOIN FETCH a.utilizador " +
           "ORDER BY p.dataPedido DESC NULLS LAST")
    List<PedidoCompra> findAllOrderByDataDesc();
}
