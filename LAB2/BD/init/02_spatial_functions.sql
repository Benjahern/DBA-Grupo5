-- Latencia proyectada entre la ubicación del usuario y cada región.
--
-- distance_m     = ST_DistanceSpheroid (WGS84, metros). Más preciso que
--                  ST_DistanceSphere (asume Tierra esférica pura).
-- latency_rtt_ms = distancia / 100000. Velocidad de la luz en fibra óptica
--                  ≈ c/1.5 ≈ 199,861 km/s ≈ 200 km/ms → RTT = 2 * d / v.
--                  Si el usuario está DENTRO de una región, distance = 0.
--
-- ORDER BY r."Geom" <-> user_point usa el GIST KNN sobre region_geom_idx:
-- el planner hace Index Scan en vez de Seq Scan cuando hay suficientes
-- filas. El <-> da distancia por bounding box, que es una buena
-- aproximación para "región más cercana" con polígonos razonables.

CREATE OR REPLACE FUNCTION fn_latencia_a_regiones(
    p_lat double precision,
    p_lng double precision
) RETURNS TABLE (
    region_id      bigint,
    region_name    varchar,
    distance_m     double precision,
    latency_rtt_ms double precision
) AS $$
DECLARE
    user_point geometry(Point, 4326);
BEGIN
    user_point := ST_SetSRID(ST_MakePoint(p_lng, p_lat), 4326);
    RETURN QUERY
    SELECT
        r."Region_id"::bigint                                       AS region_id,
        r."Name"::varchar                                           AS region_name,
        ST_DistanceSpheroid(user_point, r."Geom")::double precision AS distance_m,
        (ST_DistanceSpheroid(user_point, r."Geom") / 100000.0)
            ::double precision                                      AS latency_rtt_ms
    FROM "Region" r
    ORDER BY r."Geom" <-> user_point;
END;
$$ LANGUAGE plpgsql STABLE;
