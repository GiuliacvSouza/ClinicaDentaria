package dal;

import model.DentistaIndisponibilidade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DentistaIndisponibilidadeRepository extends JpaRepository<DentistaIndisponibilidade, Integer> {

    List<DentistaIndisponibilidade> findByDentistaIdAndAtivaTrueOrderByDataInicioAsc(Integer dentistaId);

    List<DentistaIndisponibilidade> findByDentistaIdAndDataInicioLessThanEqualAndDataFimGreaterThanEqualAndAtivaTrue(
            Integer dentistaId, LocalDate fim, LocalDate inicio);

    List<DentistaIndisponibilidade> findByDentistaIdAndDataInicioLessThanEqualAndAtivaTrue(
            Integer dentistaId, LocalDate data);
}