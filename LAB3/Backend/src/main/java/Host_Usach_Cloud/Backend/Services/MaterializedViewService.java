package Host_Usach_Cloud.Backend.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MaterializedViewService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Se ejecuta cada 2 minutos (120,000 milisegundos)
    @Scheduled(fixedRate = 120000)
    public void refreshMaterializedView() {
        try {
            // Llamamos a la función creada en materialized_view.sql
            jdbcTemplate.execute("SELECT refrescar_vista_recursos();");
            System.out.println("LOG: Vista materializada de recursos actualizada correctamente.");
        } catch (Exception e) {
            System.err.println("ERROR: No se pudo refrescar la vista: " + e.getMessage());
        }
    }
}
