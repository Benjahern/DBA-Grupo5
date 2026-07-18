-- Vista materializada
DROP MATERIALIZED VIEW IF EXISTS vista_recursos_globales CASCADE;
CREATE MATERIALIZED VIEW vista_recursos_globales AS
SELECT
    r."Name" AS region_name,
    ST_AsGeoJSON(r."Geom") AS region_geometry,
    ST_X(ST_Centroid(r."Geom")) AS centroid_lng,
    ST_Y(ST_Centroid(r."Geom")) AS centroid_lat,
    SUM(ram."Quantity") AS total_ram,
    SUM(cpu."Quantity") AS total_cpu,
    SUM(st."Quantity") AS total_storage
FROM "Instance" i
         JOIN "Region" r ON i."Region_id" = r."Region_id"
         JOIN "Ram" ram ON i."Ram_id" = ram."Ram_id"
         JOIN "CPU" cpu ON i."Cpu_id" = cpu."Cpu_id"
         JOIN "Storage" st ON i."Storage_id" = st."Storage_id"
WHERE i."State" = 'Running'
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