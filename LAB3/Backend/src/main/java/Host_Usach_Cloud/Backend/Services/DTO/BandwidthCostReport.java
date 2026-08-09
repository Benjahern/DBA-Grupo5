package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el resultado del Pipeline 1 (Aggregation $group):
 * consumo de ancho de banda y costo asociado por cliente y periodo.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BandwidthCostReport {

    private Long userId;
    private String billingPeriod;

    /** Total de bytes recibidos (download) en el periodo */
    private Long totalBytesIn;

    /** Total de bytes transmitidos (upload) en el periodo */
    private Long totalBytesOut;

    /** Total de bytes (in + out) en el periodo */
    private Long totalBytes;

    /** Total en GB (totalBytes / 1024^3) */
    private Double totalGb;

    /** Costo calculado del ancho de banda según pricing escalonado */
    private Double bandwidthCost;

    /** Cantidad de registros de medición en el periodo */
    private Integer recordCount;

    /** Cantidad de instancias distintas del usuario */
    private Integer instanceCount;
}
