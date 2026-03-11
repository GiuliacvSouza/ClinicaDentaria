package dal;
import model.Assistente;
import model.enums.NivelFormacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AssistenteRepository extends JpaRepository<Assistente, Integer> {
    List<Assistente> findByAtivo(Boolean ativo);
    List<Assistente> findByNivelFormacao(NivelFormacao nivelFormacao);
}
