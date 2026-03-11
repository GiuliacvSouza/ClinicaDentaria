package dal;
import model.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Integer> {
    Optional<Atendimento> findByIdConsulta_Id(Integer idConsulta);

    // histórico de atendimentos de um paciente (RF04)
    @Query("SELECT a FROM Atendimento a WHERE a.idConsulta.idPaciente.id = :idPaciente")
    List<Atendimento> findByPacienteId(@Param("idPaciente") Integer idPaciente);
}