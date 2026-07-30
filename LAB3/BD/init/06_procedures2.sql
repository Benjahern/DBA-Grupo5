-- Procedimiento generate_monthly_tickets(p_user_id) re-implementado:
-- "Instance" ya no existe en Postgres (vive en Mongo), por lo que el cálculo
-- de uso mensual debe hacerse en la capa de aplicación. La facturación se
-- resuelve con BillingService.generateMonthlyTickets(userId) usando un job
-- @Scheduled en el Backend que arma los Tickets desde Mongo + catálogos
-- Postgres (CPU/RAM/Storage). El procedimiento queda como no-op por
-- compatibilidad con BillingService.generateMonthlyTickets (que aún lo invoca).
CREATE OR REPLACE PROCEDURE generate_monthly_tickets(p_user_id BIGINT)
LANGUAGE plpgsql AS $$
BEGIN
    -- No-op: la facturación se realiza desde la aplicación leyendo Mongo.
    RAISE NOTICE 'generate_monthly_tickets: no-op. La facturación mensual real corre en BillingService (app-side).';
END;
$$;