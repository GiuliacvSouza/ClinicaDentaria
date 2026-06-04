package dal;
import model.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findByNomeContainingIgnoreCase(String nome);
    List<Material> findByAtivo(Boolean ativo);
    List<Material> findByAtivoTrueOrderByNomeAsc();
    List<Material> findByNomeContainingIgnoreCaseAndAtivoTrueOrderByNomeAsc(String nome);
    List<Material> findByIdFornecedor_Id(Integer idFornecedor);
    boolean existsByCodigoInternoIgnoreCase(String codigoInterno);
    boolean existsByCodigoInternoIgnoreCaseAndIdNot(String codigoInterno, Integer id);

    // materiais abaixo do stock mínimo (RF34)
    @Query("SELECT m FROM Material m WHERE m.quantidadeAtual <= m.quantidadeMinima")
    List<Material> findAbaixoStockMinimo();
}
