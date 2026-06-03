package dal;
import model.Fatura;
import model.enums.EstadoFatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FaturaRepository extends JpaRepository<Fatura, Integer> {
    // Retornar possivelmente multiplas faturas para um mesmo atendimento (protege contra dados duplicados)
    List<Fatura> findByIdAtendimento_IdOrderByDataEmissaoDesc(Integer idAtendimento);
    List<Fatura> findByEstado(EstadoFatura estado);

    @Query("""
        SELECT DISTINCT f FROM Fatura f
        LEFT JOIN FETCH f.idAtendimento a
        LEFT JOIN FETCH a.idConsulta c
        LEFT JOIN FETCH c.idPaciente p
        LEFT JOIN FETCH p.utilizador u
        WHERE f.id = :id
    """)
    Optional<Fatura> buscarFaturaCompletaParaPdf(@Param("id") Integer id);
}