-- Migración idempotente: agrega la columna Password_hash a "Users"
-- Se ejecuta en el primer arranque del volumen de Postgres (docker-entrypoint-initdb.d).
-- Si la columna ya existe, no hace nada.

ALTER TABLE "Users" ADD COLUMN IF NOT EXISTS "Password_hash" VARCHAR(255);
