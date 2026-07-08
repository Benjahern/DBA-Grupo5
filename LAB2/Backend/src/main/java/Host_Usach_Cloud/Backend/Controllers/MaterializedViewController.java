package Host_Usach_Cloud.Backend.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/reports")
public class MaterializedViewController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/global-resources")
    public List<Map<String, Object>> getGlobalResources() {
        // Refrescamos la vista materializada antes de consultarla para asegurar datos actualizados
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY vista_recursos_globales");
        } catch (Exception e) {
            // Si falla el refresco concurrente (ej: no hay datos), intentamos sin CONCURRENTLY
            try {
                jdbcTemplate.execute("REFRESH MATERIALIZED VIEW vista_recursos_globales");
            } catch (Exception ignored) {
                // Si aún falla, devolvemos los datos cacheados
            }
        }
        return jdbcTemplate.queryForList("SELECT * FROM vista_recursos_globales");
    }
}
