-- Procedimiento: genera tickets mensuales por instancia
-- Ejecutar con un scheduler (ej: dia 1 a las 00:00)
CREATE OR REPLACE PROCEDURE generate_monthly_tickets(p_user_id BIGINT)
LANGUAGE plpgsql AS $$
DECLARE
    inst RECORD;
    usage_to_bill INTERVAL;
    price_per_hour REAL;
    total_price REAL;
BEGIN
    FOR inst IN
        SELECT i."Instance_id", i."Active_hours", i."State", i."Started_at",
               (c."Cost_ph" + r."Cost_ph" + s."Cost_ph") AS total_cost_ph
        FROM "Instance" i
        JOIN "CPU" c ON i."Cpu_id" = c."Cpu_id"
        JOIN "Ram" r ON i."Ram_id" = r."Ram_id"
        JOIN "Storage" s ON i."Storage_id" = s."Storage_id"
        WHERE i."User_id" = p_user_id
    LOOP
        usage_to_bill := COALESCE(inst."Active_hours", INTERVAL '0 seconds');

        IF inst."State" = 'Running' AND inst."Started_at" IS NOT NULL THEN
            usage_to_bill := usage_to_bill + (NOW() - inst."Started_at");
        END IF;

        price_per_hour := inst.total_cost_ph;
        total_price := (EXTRACT(EPOCH FROM usage_to_bill) / 3600.0) * price_per_hour;

        INSERT INTO "Ticket" ("Instance_id", "Usage", "Price")
        VALUES (inst."Instance_id", usage_to_bill, total_price);

        -- Reiniciar contadores para el siguiente ciclo mensual
        IF inst."State" = 'Running' THEN
            UPDATE "Instance"
            SET "Active_hours" = INTERVAL '0 seconds',
                "Started_at" = NOW()
            WHERE "Instance_id" = inst."Instance_id";
        ELSE
            UPDATE "Instance"
            SET "Active_hours" = INTERVAL '0 seconds',
                "Started_at" = NULL
            WHERE "Instance_id" = inst."Instance_id";
        END IF;
    END LOOP;
END;
$$;
