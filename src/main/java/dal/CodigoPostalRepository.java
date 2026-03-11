package dal;
import model.CodigoPostal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CodigoPostalRepository extends JpaRepository<CodigoPostal, String> {
    // String porque o id é String ex: "4700-000"
    List<CodigoPostal> findByLocalidadeContainingIgnoreCase(String localidade);
}