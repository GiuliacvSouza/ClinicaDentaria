package dal;
import model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Integer> {
    List<Medicamento> findByNomeContainingIgnoreCase(String nome);
    List<Medicamento> findByFabricante(String fabricante);
}