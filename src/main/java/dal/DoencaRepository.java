package dal;
import model.Doenca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DoencaRepository extends JpaRepository<Doenca, Integer> {
    List<Doenca> findByNomeContainingIgnoreCase(String nome);
    List<Doenca> findByCategoria(String categoria);
    List<Doenca> findByAtiva(Boolean ativa);
}