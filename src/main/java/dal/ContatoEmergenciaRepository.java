package dal;
import model.ContatoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ContatoEmergenciaRepository extends JpaRepository<ContatoEmergencia, Integer> {
    // buscar contactos de emergência de um paciente
    List<ContatoEmergencia> findByPaciente_Id(Integer idPaciente);
}