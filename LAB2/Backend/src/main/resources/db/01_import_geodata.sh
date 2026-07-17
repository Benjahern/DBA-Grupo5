#!/bin/bash
set -e

# Instalar gdal (necesario si no está en la imagen)
apk add --no-cache gdal

# 1. Definir explícitamente un directorio temporal donde postgres SÍ tiene acceso
export TMPDIR=/tmp
export GDAL_DATA=/usr/share/gdal

# 2. Desactivar logs de forma agresiva
# CPL_LOG /dev/null redirige el log al "agujero negro" de Linux
# OGR_SKIP_LOG TRUE impide que se generen logs internos
export CPL_LOG=/dev/null
export OGR_SKIP_LOG=TRUE

# 3. Ejecutar ogr2ogr con las configuraciones de silencio
ogr2ogr -f "PostgreSQL" \
        PG:"dbname=$POSTGRES_DB user=$POSTGRES_USER password=$POSTGRES_PASSWORD" \
        /docker-entrypoint-initdb.d/data/PB2002_plates.json \
        -nln "RiskZone" \
        -append \
        -lco GEOMETRY_NAME=Geom \
        -nlt PROMOTE_TO_MULTI \
        --config CPL_LOG /dev/null

# 4. Normalizar datos
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    UPDATE "RiskZone" 
    SET "Metadata" = jsonb_build_object(
        'PlateName', platename,
        'Layer', layer,
        'Code', code
    )
    WHERE "Metadata" IS NULL;
EOSQL