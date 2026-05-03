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
        // Consultamos la vista materializada, no las tablas base. ¡Velocidad pura!
        return jdbcTemplate.queryForList("SELECT * FROM vista_recursos_globales");
    }
}
