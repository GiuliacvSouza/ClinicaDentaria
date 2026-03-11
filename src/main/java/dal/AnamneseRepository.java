package dal;
import model.Anamnese;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AnamneseRepository extends JpaRepository<Anamnese, Integer> {
    Optional<Anamnese> findByIdAtendimento_Id(Integer idAtendimento);
}