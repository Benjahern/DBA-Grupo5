-- Trigger 1: Bloquear la creación de un servidor si el usuario ha superado el límite de su cuota contratada.

CREATE OR REPLACE FUNCTION check_user_quota()
RETURNS TRIGGER AS $$
DECLARE
    v_max_instances INTEGER;
    v_current_instances INTEGER;
BEGIN
    -- Obtener la cuota máxima del usuario
    SELECT "Max_instances" INTO v_max_instances
    FROM "Users"
    WHERE "User_id" = NEW."User_id";

    -- Contar las instancias actuales del usuario (que no estén terminadas)
    SELECT COUNT(*) INTO v_current_instances
    FROM "Instance"
    WHERE "User_id" = NEW."User_id" AND "Terminated" = FALSE;

    -- Verificar si excede la cuota
    IF v_current_instances >= v_max_instances THEN
        RAISE EXCEPTION 'El usuario ha superado el límite de su cuota contratada de instancias (Max: %)', v_max_instances;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_quota
BEFORE INSERT ON "Instance"
FOR EACH ROW
EXECUTE FUNCTION check_user_quota();


-- Trigger 2: Liberar automáticamente la dirección IP asociada a un servidor en el momento en que este sea eliminado (destruido).
CREATE OR REPLACE FUNCTION release_instance_ip()
RETURNS TRIGGER AS $$
BEGIN

    -- Manejo para eliminación lógica a través de actualización de estado
    IF TG_OP = 'UPDATE' AND NEW."Terminated" = TRUE AND OLD."Terminated" = FALSE THEN
        -- Marcar la IP como no asignada en la tabla Ip
        IF OLD."Ip_address" IS NOT NULL THEN
            UPDATE "Ip" SET "Assigned" = FALSE WHERE "Ip_address" = OLD."Ip_address";
        END IF;
        NEW."Ip_address" := NULL;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Se asocia a BEFORE UPDATE para poder sobreescribir el valor antes de guardar el estado
CREATE TRIGGER trg_release_ip
BEFORE UPDATE ON "Instance"
FOR EACH ROW
EXECUTE FUNCTION release_instance_ip();


-- =========== Auxiliares ==============================

-- Trigger 3: Calcular el tiempo de uso (Active_hours) cuando una instancia pasa de Running a Stopped / Terminated
CREATE OR REPLACE FUNCTION calculate_active_hours()
RETURNS TRIGGER AS $$
BEGIN
    -- De Running a Stopped / Terminated
    IF OLD."State" = 'Running' AND NEW."State" IN ('Stopped', 'Terminated') THEN
        IF OLD."Started_at" IS NOT NULL THEN
            -- Sumar el lapso actual a las horas ya acumuladas
            NEW."Active_hours" := COALESCE(OLD."Active_hours", INTERVAL '0 seconds') + (NOW() - OLD."Started_at");
        END IF;
        -- Resetear el inicio
        NEW."Started_at" := NULL;
    END IF;

    -- De Stopped a Running (Para empezar a contar otra vez)
    IF OLD."State" IN ('Stopped', 'Terminated') AND NEW."State" = 'Running' THEN
        NEW."Started_at" := NOW();
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_active_hours
BEFORE UPDATE ON "Instance"
FOR EACH ROW
WHEN (OLD."State" IS DISTINCT FROM NEW."State")
EXECUTE FUNCTION calculate_active_hours();

