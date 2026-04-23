package app;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class FaturaSchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public FaturaSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void ajustarSchemaFatura() {
        jdbcTemplate.execute("ALTER TABLE fatura ADD COLUMN IF NOT EXISTS valor_base numeric(10,2)");
        jdbcTemplate.execute("ALTER TABLE fatura ADD COLUMN IF NOT EXISTS taxa_iva numeric(5,2)");

        jdbcTemplate.execute(
                "UPDATE fatura " +
                "SET valor_base = COALESCE(valor_base, valor_final, 0)"
        );

        jdbcTemplate.execute(
                "UPDATE fatura " +
                "SET taxa_iva = COALESCE(taxa_iva, 0)"
        );

        jdbcTemplate.execute(
                "UPDATE fatura " +
                "SET valor_final = ROUND((COALESCE(valor_base, 0) * (1 + COALESCE(taxa_iva, 0) / 100.0))::numeric, 2) " +
                "WHERE valor_final IS NULL"
        );

        jdbcTemplate.execute("ALTER TABLE fatura ALTER COLUMN valor_base SET NOT NULL");
        jdbcTemplate.execute("ALTER TABLE fatura ALTER COLUMN taxa_iva SET NOT NULL");
    }
}
