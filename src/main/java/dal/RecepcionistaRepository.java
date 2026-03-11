package dal;
import model.Recepcionista;
import model.enums.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RecepcionistaRepository extends JpaRepository<Recepcionista, Integer> {
    List<Recepcionista> findByTurno(Turno turno);
}
