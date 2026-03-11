package dal;
import model.Seguro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeguroRepository extends JpaRepository<Seguro, Integer> {
    List<Seguro> findByNomeSeguroContainingIgnoreCase(String nome);
    List<Seguro> findByTipoPlano(String tipoPlano);
}