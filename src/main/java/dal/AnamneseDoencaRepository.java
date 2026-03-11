package dal;
import model.AnamneseDoenca;
import model.AnamneseDoencaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnamneseDoencaRepository
        extends JpaRepository<AnamneseDoenca, AnamneseDoencaId> {
    List<AnamneseDoenca> findByIdAnamnese_Id(Integer idAnamnese);
    List<AnamneseDoenca> findByIdDoenca_Id(Integer idDoenca);
}