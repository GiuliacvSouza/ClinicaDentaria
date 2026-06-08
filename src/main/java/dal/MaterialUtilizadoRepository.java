package dal;

import model.MaterialUtilizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialUtilizadoRepository extends JpaRepository<MaterialUtilizado, Integer> {

    @Query("""
            SELECT mu
            FROM MaterialUtilizado mu
            LEFT JOIN FETCH mu.material
            LEFT JOIN FETCH mu.atendimento a
            LEFT JOIN FETCH a.idConsulta c
            WHERE a.id = :idAtendimento
            ORDER BY mu.id DESC
            """)
    List<MaterialUtilizado> findByAtendimentoIdComMaterial(@Param("idAtendimento") Integer idAtendimento);
}
