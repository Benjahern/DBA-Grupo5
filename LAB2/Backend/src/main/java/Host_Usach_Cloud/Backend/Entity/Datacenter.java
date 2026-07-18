package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Datacenter {

    private Long id;

    private String name;

    private DatacenterStatus status;

    public enum DatacenterStatus {
        OPERATIVO,
        MANTENIMIENTO,
        DEGRADADO,
        FUERA_DE_SERVICIO
    }

    private Integer currentInstances;

    private Integer capacity;

    // Ubicación
    private Double latitude;
    private Double longitude;

    // Relaciones
    private Long regionId;

    // Placa tectónica donde está ubicado
    private Long riskZoneId;
}
