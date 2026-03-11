package dal;
import model.AtendimentoProcedimento;
import model.AtendimentoProcedimentoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AtendimentoProcedimentoRepository
        extends JpaRepository<AtendimentoProcedimento, AtendimentoProcedimentoId> {
    List<AtendimentoProcedimento> findByIdAtendimento_Id(Integer idAtendimento);
    List<AtendimentoProcedimento> findByIdProcedimento_Id(Integer idProcedimento);
}