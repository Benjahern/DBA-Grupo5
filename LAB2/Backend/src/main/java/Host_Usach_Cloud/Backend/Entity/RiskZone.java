package Host_Usach_Cloud.Backend.Entity;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Polygon;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiskZone {

    private Long Zona_id;
    private String Name;
    private String Type;
    private Integer Severity_level;

    // Nueva columna JSONB
    private JsonNode Metadata;     // Representa el objeto JSONB de Postgres

    private Polygon Geom;
}