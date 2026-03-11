package dal;
import model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {
    List<MovimentacaoEstoque> findByIdMaterial_Id(Integer idMaterial);
    List<MovimentacaoEstoque> findByIdUtilizador_Id(Integer idAssistente);
    List<MovimentacaoEstoque> findByData(LocalDate data);
    List<MovimentacaoEstoque> findByDataBetween(LocalDate inicio, LocalDate fim);
}