ALTER TABLE IF EXISTS fatura
    ADD COLUMN IF NOT EXISTS valor_base numeric(10,2)
@@

ALTER TABLE IF EXISTS fatura
    ADD COLUMN IF NOT EXISTS taxa_iva numeric(5,2)
@@

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'fatura'
    ) THEN
        IF EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = 'fatura'
              AND column_name = 'valor_final'
        ) THEN
            EXECUTE 'UPDATE fatura SET valor_base = COALESCE(valor_base, valor_final, 0)';
        ELSE
            EXECUTE 'UPDATE fatura SET valor_base = COALESCE(valor_base, 0)';
        END IF;
    END IF;
END
$$
@@

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'fatura'
    ) THEN
        UPDATE fatura
        SET taxa_iva = COALESCE(taxa_iva, 0);
    END IF;
END
$$
@@

ALTER TABLE IF EXISTS fatura
    ALTER COLUMN valor_base SET NOT NULL
@@

ALTER TABLE IF EXISTS fatura
    ALTER COLUMN taxa_iva SET NOT NULL
@@

ALTER TABLE IF EXISTS fatura
    ALTER COLUMN taxa_iva SET DEFAULT 0
@@

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = current_schema()
          AND table_name = 'fatura'
    ) THEN
        IF NOT EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = current_schema()
              AND table_name = 'fatura'
              AND column_name = 'valor_final'
              AND is_generated <> 'NEVER'
        ) THEN
            ALTER TABLE IF EXISTS fatura
                DROP COLUMN IF EXISTS valor_final;

            ALTER TABLE IF EXISTS fatura
                ADD COLUMN valor_final numeric(10,2)
                GENERATED ALWAYS AS (
                    ROUND((COALESCE(valor_base, 0) * (1 + COALESCE(taxa_iva, 0) / 100.0))::numeric, 2)
                ) STORED;
        END IF;
    END IF;
END
$$
@@

ALTER TABLE IF EXISTS consulta
    DROP CONSTRAINT IF EXISTS consulta_status_check
@@

ALTER TABLE IF EXISTS consulta
    ADD CONSTRAINT consulta_status_check
    CHECK (status IN (
        'AGENDADA',
        'CONFIRMADA',
        'EM_ATENDIMENTO',
        'CONCLUIDA',
        'FATURADA',
        'CANCELADA',
        'FALTA',
        'PENDENTE',
        'EM_ESPERA',
        'EM_CONSULTA'
    ))
@@
