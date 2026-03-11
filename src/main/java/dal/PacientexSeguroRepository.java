package dal;
import model.PacientexSeguro;
import model.PacientexSeguroId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PacientexSeguroRepository
        extends JpaRepository<PacientexSeguro, PacientexSeguroId> {
    List<PacientexSeguro> findByIdUtilizador_Id(Integer idPaciente);
    List<PacientexSeguro> findByIdSeguro_Id(Integer idSeguro);
}