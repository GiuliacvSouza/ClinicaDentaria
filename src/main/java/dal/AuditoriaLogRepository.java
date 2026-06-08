package dal;

import model.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, Integer> {
    List<AuditoriaLog> findByUtilizadorNomeContainingIgnoreCase(String utilizadorNome);
    List<AuditoriaLog> findByOperacaoContainingIgnoreCase(String operacao);
    List<AuditoriaLog> findByDataHoraBetween(LocalDateTime inicio, LocalDateTime fim);
    List<AuditoriaLog> findAllByOrderByDataHoraDesc();
}