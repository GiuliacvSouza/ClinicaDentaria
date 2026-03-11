package dal;
import model.AnamneseMedicamento;
import model.AnamneseMedicamentoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnamneseMedicamentoRepository
        extends JpaRepository<AnamneseMedicamento, AnamneseMedicamentoId> {
    List<AnamneseMedicamento> findByIdAnamnese_Id(Integer idAnamnese);
    List<AnamneseMedicamento> findByIdMedicamento_Id(Integer idMedicamento);
}