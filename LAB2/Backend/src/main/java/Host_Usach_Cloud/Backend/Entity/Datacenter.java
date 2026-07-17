package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Datacenter {

    private Long Datacenter_id;
    private String Name;
    private String Status;       // OPERATIVO, MANTENIMIENTO, etc.
    private Integer Capacity;

    // Relación basada en ID
    private Long Region_id;

    // Punto geométrico (PostGIS)
    private Point Geom;
}
