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

    // Carrega todos os materiais com o fornecedor num único query (evita LazyInitializationException)
    @Query("SELECT m FROM Material m LEFT JOIN FETCH m.idFornecedor ORDER BY m.nome ASC NULLS LAST")
    List<Material> findAllWithFornecedor();

    // materiais abaixo do stock mínimo (RF34) — com fornecedor para os alertas
    @Query("SELECT m FROM Material m LEFT JOIN FETCH m.idFornecedor WHERE m.quantidadeAtual <= m.quantidadeMinima")
    List<Material> findAbaixoStockMinimo();
}
