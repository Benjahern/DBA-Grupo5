package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para el resultado del Pipeline 2 (Aggregation $bucket):
 * distribución de clientes por rangos de consumo de ancho de banda.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BandwidthBucketReport {

    /** Límite inferior del bucket en GB */
    private Double bucketMinGb;

    /** Límite superior del bucket en GB (null = infinito para el último bucket) */
    private Double bucketMaxGb;

    /** Etiqueta legible del rango (ej: "0 – 1 GB") */
    private String bucketLabel;

    /** Cantidad de clientes en este rango */
    private Integer clientCount;

    /** Total de GB consumidos por todos los clientes en este rango */
    private Double totalGbInBucket;

    /** Costo total generado por los clientes en este rango */
    private Double totalCostInBucket;
}
