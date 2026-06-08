package dal;
import model.Prontuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ProntuarioRepository extends JpaRepository<Prontuario, Integer> {
    Optional<Prontuario> findByPaciente_Id(Integer idPaciente);

    @Query("SELECT pr FROM Prontuario pr LEFT JOIN FETCH pr.paciente p LEFT JOIN FETCH p.utilizador WHERE p.id = :idPaciente")
    Optional<Prontuario> findByPacienteIdComUtilizador(@Param("idPaciente") Integer idPaciente);
}
