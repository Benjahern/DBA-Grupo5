#!/bin/bash
set -e

DATA_PATH="/tmp/data/PB2002_plates.json"
# Cambiamos la ruta de salida a /tmp/ (que es escribible por el usuario postgres)
SQL_DUMP="/tmp/import.sql"

echo "Generando archivo SQL intermedio en /tmp/import.sql..."

# 1. Generamos el dump en /tmp/
ogr2ogr -f "PGDUMP" \
  "$SQL_DUMP" \
  "$DATA_PATH" \
  -nln "RiskZone" \
  -nlt PROMOTE_TO_MULTI \
  -overwrite

echo "Cargando datos a la base de datos..."

# 2. Cargamos el archivo generado
psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f "$SQL_DUMP"

echo "Importación finalizada exitosamente."