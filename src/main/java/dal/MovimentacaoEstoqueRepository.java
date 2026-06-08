package dal;

import model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Integer> {

    // JOIN FETCH para evitar LazyInitializationException no FX thread
    @Query("SELECT m FROM MovimentacaoEstoque m " +
           "LEFT JOIN FETCH m.idMaterial mat " +
           "LEFT JOIN FETCH mat.idFornecedor " +
           "LEFT JOIN FETCH m.idUtilizador a " +
           "LEFT JOIN FETCH a.utilizador " +
           "ORDER BY m.data DESC NULLS LAST")
    List<MovimentacaoEstoque> findAllByOrderByDataDesc();

    List<MovimentacaoEstoque> findByIdMaterial_Id(Integer idMaterial);

    @Query("SELECT m FROM MovimentacaoEstoque m " +
           "LEFT JOIN FETCH m.idMaterial mat " +
           "LEFT JOIN FETCH mat.idFornecedor " +
           "LEFT JOIN FETCH m.idUtilizador a " +
           "LEFT JOIN FETCH a.utilizador " +
           "WHERE LOWER(mat.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
           "ORDER BY m.data DESC NULLS LAST")
    List<MovimentacaoEstoque> findByIdMaterial_NomeContainingIgnoreCaseOrderByDataDesc(@Param("nome") String nome);

    List<MovimentacaoEstoque> findByIdUtilizador_Id(Integer idAssistente);

    List<MovimentacaoEstoque> findByData(LocalDate data);

    @Query("SELECT m FROM MovimentacaoEstoque m " +
           "LEFT JOIN FETCH m.idMaterial mat " +
           "LEFT JOIN FETCH mat.idFornecedor " +
           "LEFT JOIN FETCH m.idUtilizador a " +
           "LEFT JOIN FETCH a.utilizador " +
           "WHERE m.data BETWEEN :inicio AND :fim " +
           "ORDER BY m.data DESC NULLS LAST")
    List<MovimentacaoEstoque> findByDataBetweenOrderByDataDesc(@Param("inicio") LocalDate inicio,
                                                               @Param("fim") LocalDate fim);
}
