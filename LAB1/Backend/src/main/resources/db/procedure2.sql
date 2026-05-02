-- Procedimiento: Aprovisionar un nuevo servidor
-- Marca la IP como ocupada y crea Ticket inicial
CREATE OR REPLACE PROCEDURE provision_instance(
    p_ip_address VARCHAR,
    p_instance_id BIGINT
)
LANGUAGE plpgsql AS $$
BEGIN
    -- Marcar IP como ocupada (Assigned = true)
    UPDATE "Ip" SET "Assigned" = TRUE WHERE "Ip_address" = p_ip_address;

    -- Crear Ticket inicial (Usage = 0, Price = 0, queda listo para cuando quiera pagar)
    INSERT INTO "Ticket" ("Instance_id", "Usage", "Price")
    VALUES (p_instance_id, INTERVAL '0 seconds', 0);
END;
$$;