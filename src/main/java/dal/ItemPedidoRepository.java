package dal;
import model.ItemPedido;
import model.ItemPedidoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, ItemPedidoId> {
    List<ItemPedido> findByIdPedido_Id(Integer idPedido);
    List<ItemPedido> findByIdMaterial_Id(Integer idMaterial);
}
