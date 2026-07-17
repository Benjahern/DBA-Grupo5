package Host_Usach_Cloud.Backend.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RiskZoneRepository {
    private final JdbcTemplate jdbcTemplate;

    public RiskZoneRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Obtiene todas las zonas de riesgo.
     * Incluye 'Metadata' en las propiedades para el frontend.
     */
    public String findAllAsGeoJson() {
        String query = """
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(feature), '[]'::jsonb)
            )
            FROM (
                SELECT jsonb_build_object(
                    'type', 'Feature',
                    'geometry', ST_AsGeoJSON("Geom")::jsonb,
                    'properties', jsonb_build_object(
                        'id', "Zona_id",
                        'name', "Name",
                        'type', "Type",
                        'severity_level', "Severity_level",
                        'metadata', "Metadata"
                    )
                ) AS feature
                FROM "RiskZone"
            ) features;
        """;

        return jdbcTemplate.queryForObject(query, String.class);
    }

    /**
     * Filtra por tipo de riesgo y devuelve GeoJSON.
     */
    public String findByTypeAsGeoJson(String type) {
        String query = """
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(feature), '[]'::jsonb)
            )
            FROM (
                SELECT jsonb_build_object(
                    'type', 'Feature',
                    'geometry', ST_AsGeoJSON("Geom")::jsonb,
                    'properties', jsonb_build_object(
                        'id', "Zona_id",
                        'name', "Name",
                        'type', "Type",
                        'severity_level', "Severity_level",
                        'metadata', "Metadata"
                    )
                ) AS feature
                FROM "RiskZone"
                WHERE "Type" = ?
            ) features;
        """;

        return jdbcTemplate.queryForObject(query, String.class, type);
    }
}
