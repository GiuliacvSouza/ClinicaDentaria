package dal;
import model.EspecialidadeDentista;
import model.EspecialidadeDentistaId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EspecialidadexDentistaRepository
        extends JpaRepository<EspecialidadeDentista, EspecialidadeDentistaId> {
    List<EspecialidadeDentista> findByIdUtilizador_Id(Integer idDentista);
    List<EspecialidadeDentista> findByIdEspecialidade_Id(Integer idEspecialidade);
}