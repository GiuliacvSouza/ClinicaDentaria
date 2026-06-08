package dal;

import model.Prescricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescricaoRepository extends JpaRepository<Prescricao, Integer> {

    @Query("""
            SELECT p
            FROM Prescricao p
            LEFT JOIN FETCH p.paciente pac
            LEFT JOIN FETCH pac.utilizador
            LEFT JOIN FETCH p.dentista d
            LEFT JOIN FETCH d.utilizador
            LEFT JOIN FETCH p.consulta
            ORDER BY p.data DESC, p.id DESC
            """)
    List<Prescricao> findAllComDetalhes();

    @Query("""
            SELECT p
            FROM Prescricao p
            LEFT JOIN FETCH p.paciente pac
            LEFT JOIN FETCH pac.utilizador
            LEFT JOIN FETCH p.dentista d
            LEFT JOIN FETCH d.utilizador
            WHERE pac.id = :idPaciente
            ORDER BY p.data DESC, p.id DESC
            """)
    List<Prescricao> findByPacienteIdComDetalhes(@Param("idPaciente") Integer idPaciente);

    @Query("""
            SELECT p
            FROM Prescricao p
            LEFT JOIN FETCH p.paciente pac
            LEFT JOIN FETCH pac.utilizador
            LEFT JOIN FETCH p.dentista d
            LEFT JOIN FETCH d.utilizador
            LEFT JOIN FETCH p.consulta c
            WHERE d.id = :idDentista
            ORDER BY p.data DESC, p.id DESC
            """)
    List<Prescricao> findByDentista_IdOrderByDataDesc(@Param("idDentista") Integer idDentista);
}