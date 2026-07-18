package Host_Usach_Cloud.Backend.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RiskZoneRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Este metodo es el que consume tu Frontend (Vue)
    public String findAllAsGeoJson() {
        String sql = """
            SELECT jsonb_build_object(
                'type', 'FeatureCollection',
                'features', COALESCE(jsonb_agg(feature), '[]'::jsonb)
            )
            FROM (
                SELECT jsonb_build_object(
                    'type', 'Feature',
                    'geometry', ST_AsGeoJSON(wkb_geometry)::jsonb,
                    'properties', jsonb_build_object(
                        'id', ogc_fid,
                        'layer', layer,
                        'code', code,
                        'name', platename
                    )
                ) AS feature
                FROM "riskzone"
            ) features;
        """;
        return jdbcTemplate.queryForObject(sql, String.class);
    }

    // Opcional: Si necesitas traer las entidades reales a Java para lógica de negocio
    // puedes implementar un RowMapper aquí.
}
