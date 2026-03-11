package dal;
import model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Integer> {
    Optional<Prontuario> findByPaciente_Id(Integer idPaciente);
}