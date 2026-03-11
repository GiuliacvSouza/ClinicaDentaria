package dal;
import model.Utilizador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UtilizadorRepository extends JpaRepository<Utilizador, Integer> {
    Optional<Utilizador> findByEmail(String email);
    Optional<Utilizador> findByNif(String nif);
    Optional<Utilizador> findByTelemovel(String telemovel);
    List<Utilizador> findByTipoUtilizador(String tipoUtilizador);
    List<Utilizador> findByPrimeiroNomeContainingIgnoreCaseOrUltimoNomeContainingIgnoreCase(
            String primeiroNome, String ultimoNome
    );
    boolean existsByEmail(String email);
    boolean existsByNif(String nif);
}