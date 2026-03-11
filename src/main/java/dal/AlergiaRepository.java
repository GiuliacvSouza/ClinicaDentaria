package dal;
import model.Alergia;
import model.enums.TipoAlergia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlergiaRepository extends JpaRepository<Alergia, Integer> {
    List<Alergia> findByTipo(TipoAlergia tipo);
    List<Alergia> findBySubstanciaContainingIgnoreCase(String substancia);
}