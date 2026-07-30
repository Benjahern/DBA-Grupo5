-- Vista materializada: ya no incluye "Instance" (la entidad vive en Mongo).
-- Se conserva el cálculo agregado de recursos por región como vista vacía
-- para no romper consumidores existentes. Si necesitas los recursos en uso,
-- agrega una colección Mongo "region_resource_stats" poblada por la app o
-- lee directamente de Mongo con $group en una aggregation pipeline.

DROP MATERIALIZED VIEW IF EXISTS vista_recursos_globales CASCADE;
CREATE MATERIALIZED VIEW vista_recursos_globales AS
SELECT
    r."Name" AS region_name,
    ST_AsGeoJSON(r."Geom") AS region_geometry,
    ST_X(ST_Centroid(r."Geom")) AS centroid_lng,
    ST_Y(ST_Centroid(r."Geom")) AS centroid_lat,
    0::BIGINT AS total_ram,
    0::BIGINT AS total_cpu,
    0::BIGINT AS total_storage
FROM "Region" r
GROUP BY r."Name", r."Geom";

-- Crear el índice para permitir refresco concurrente en el futuro
CREATE UNIQUE INDEX IF NOT EXISTS idx_region_name ON vista_recursos_globales (region_name);

-- Crear función para refrescar
CREATE OR REPLACE FUNCTION refrescar_vista_recursos()
RETURNS void AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY vista_recursos_globales;
END;
$$ LANGUAGE plpgsql;