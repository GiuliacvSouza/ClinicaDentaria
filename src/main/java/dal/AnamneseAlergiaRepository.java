package dal;
import model.AnamneseAlergia;
import model.AnamneseAlergiaId;
import model.enums.Gravidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnamneseAlergiaRepository
        extends JpaRepository<AnamneseAlergia, AnamneseAlergiaId> {
    List<AnamneseAlergia> findByIdAnamnese_Id(Integer idAnamnese);
    List<AnamneseAlergia> findByIdAlergia_Id(Integer idAlergia);
    List<AnamneseAlergia> findByGravidade(Gravidade gravidade);
}