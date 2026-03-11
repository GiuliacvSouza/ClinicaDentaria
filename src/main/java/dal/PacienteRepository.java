package dal;
import model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Integer> {
    // buscar por NIF (RF15)
    @Query("SELECT p FROM Paciente p WHERE p.utilizador.nif = :nif")
    Optional<Paciente> findByNif(@Param("nif") String nif);
    // buscar por telemóvel (RF15)
    @Query("SELECT p FROM Paciente p WHERE p.utilizador.telemovel = :telemovel")
    Optional<Paciente> findByTelemovel(@Param("telemovel") String telemovel);
    // buscar por nome (RF15)
    @Query("SELECT p FROM Paciente p WHERE LOWER(p.utilizador.primeiroNome) LIKE LOWER(CONCAT('%', :nome, '%')) OR LOWER(p.utilizador.ultimoNome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Paciente> findByNome(@Param("nome") String nome);
    List<Paciente> findByStatus(String status);
}