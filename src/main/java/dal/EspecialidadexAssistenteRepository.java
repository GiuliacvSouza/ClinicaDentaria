package dal;
import model.EspecialidadexAssistente;
import model.EspecialidadexAssistenteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EspecialidadexAssistenteRepository
        extends JpaRepository<EspecialidadexAssistente, EspecialidadexAssistenteId> {
    List<EspecialidadexAssistente> findByIdUtilizador_Id(Integer idAssistente);
    List<EspecialidadexAssistente> findByIdEspecialidade_Id(Integer idEspecialidade);
}