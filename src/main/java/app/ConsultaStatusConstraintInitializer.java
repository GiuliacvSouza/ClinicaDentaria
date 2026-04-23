package app;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConsultaStatusConstraintInitializer {

    private final JdbcTemplate jdbcTemplate;

    public ConsultaStatusConstraintInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void atualizarConstraintStatusConsulta() {
        jdbcTemplate.execute("ALTER TABLE consulta DROP CONSTRAINT IF EXISTS consulta_status_check");
        jdbcTemplate.execute(
                "ALTER TABLE consulta ADD CONSTRAINT consulta_status_check " +
                "CHECK (status IN (" +
                "'AGENDADA'," +
                "'CONFIRMADA'," +
                "'EM_ATENDIMENTO'," +
                "'CONCLUIDA'," +
                "'CANCELADA'," +
                "'FALTA'," +
                "'PENDENTE'," +
                "'EM_ESPERA'," +
                "'EM_CONSULTA'" +
                "))"
        );
    }
}
