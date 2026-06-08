package dal;

import model.PlanoTratamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoTratamentoRepository extends JpaRepository<PlanoTratamento, Integer> {

    @Query("""
            SELECT p
            FROM PlanoTratamento p
            LEFT JOIN FETCH p.paciente pac
            LEFT JOIN FETCH pac.utilizador
            LEFT JOIN FETCH p.dentista d
            LEFT JOIN FETCH d.utilizador
            ORDER BY p.id DESC
            """)
    List<PlanoTratamento> findAllComDetalhes();

    @Query("""
            SELECT p
            FROM PlanoTratamento p
            LEFT JOIN FETCH p.paciente pac
            LEFT JOIN FETCH pac.utilizador
            LEFT JOIN FETCH p.dentista d
            LEFT JOIN FETCH d.utilizador
            WHERE pac.id = :idPaciente
            ORDER BY p.id DESC
            """)
    List<PlanoTratamento> findByPacienteIdComDetalhes(@Param("idPaciente") Integer idPaciente);

    List<PlanoTratamento> findByDentista_IdOrderByIdDesc(Integer idDentista);
}
