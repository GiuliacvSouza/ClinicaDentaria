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

    @Query("""
            SELECT DISTINCT a
            FROM Atendimento a
            LEFT JOIN FETCH a.idConsulta c
            LEFT JOIN FETCH c.idPaciente p
            LEFT JOIN FETCH p.utilizador
            LEFT JOIN FETCH c.idDentista d
            LEFT JOIN FETCH d.utilizador
            LEFT JOIN FETCH a.procedimentos ap
            LEFT JOIN FETCH ap.idProcedimento
            WHERE a.id = :id
            """)
    Optional<Atendimento> findByIdComDetalhes(@Param("id") Integer id);

    @Query("""
            SELECT DISTINCT a
            FROM Atendimento a
            LEFT JOIN FETCH a.idConsulta c
            LEFT JOIN FETCH c.idPaciente p
            LEFT JOIN FETCH p.utilizador
            LEFT JOIN FETCH c.idDentista d
            LEFT JOIN FETCH d.utilizador
            LEFT JOIN FETCH a.procedimentos ap
            LEFT JOIN FETCH ap.idProcedimento
            WHERE c.id = :idConsulta
            ORDER BY a.id DESC
            """)
    List<Atendimento> findByConsultaIdComDetalhes(@Param("idConsulta") Integer idConsulta);

    @Query("SELECT a FROM Atendimento a WHERE a.idConsulta.idPaciente.id = :idPaciente")
    List<Atendimento> findByPacienteId(@Param("idPaciente") Integer idPaciente);

    @Query("""
            SELECT DISTINCT a
            FROM Atendimento a
            LEFT JOIN FETCH a.idConsulta c
            LEFT JOIN FETCH c.idPaciente p
            LEFT JOIN FETCH p.utilizador
            LEFT JOIN FETCH c.idDentista d
            LEFT JOIN FETCH d.utilizador
            LEFT JOIN FETCH a.procedimentos ap
            LEFT JOIN FETCH ap.idProcedimento
            WHERE p.id = :idPaciente
            ORDER BY c.dataHoraInicio DESC
            """)
    List<Atendimento> findByPacienteIdComDetalhes(@Param("idPaciente") Integer idPaciente);
}
