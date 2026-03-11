package dal;
import model.Consulta;
import model.enums.EstadoConsulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;

@Repository
public interface ConsultaRepository extends JpaRepository<Consulta, Integer> {
    // consultas de um paciente (RF36)
    List<Consulta> findByIdPaciente_Id(Integer idPaciente);
    // agenda do dentista (RF18)
    List<Consulta> findByIdDentista_Id(Integer idDentista);
    // consultas por estado (RF16)
    List<Consulta> findByStatus(EstadoConsulta status);
    // consultas de um dentista num dia (RF16, RF18)
    @Query("SELECT c FROM Consulta c WHERE c.idDentista.id = :idDentista AND c.dataHoraInicio BETWEEN :inicio AND :fim")
    List<Consulta> findByDentistaEDia(
            @Param("idDentista") Integer idDentista,
            @Param("inicio") Instant inicio,
            @Param("fim") Instant fim
    );
    // verificar disponibilidade de horário (RF02)
    @Query("SELECT c FROM Consulta c WHERE c.idDentista.id = :idDentista AND c.dataHoraInicio = :dataHora AND c.status != 'CANCELADA'")
    List<Consulta> findConflitoHorario(
            @Param("idDentista") Integer idDentista,
            @Param("dataHora") Instant dataHora
    );
}