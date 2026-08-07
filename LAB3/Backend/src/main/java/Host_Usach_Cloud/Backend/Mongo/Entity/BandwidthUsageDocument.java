package Host_Usach_Cloud.Backend.Mongo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("bandwidth_usage")
@CompoundIndex(name = "user_period_idx", def = "{'userId': 1, 'billingPeriod': 1}")
@CompoundIndex(name = "instance_period_idx", def = "{'instanceId': 1, 'billingPeriod': 1}")
public class BandwidthUsageDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    /** numericId de la instancia (sequential per-user) */
    private Long instanceId;

    /** Bytes recibidos (download) en esta muestra */
    private Long bytesIn;

    /** Bytes transmitidos (upload) en esta muestra */
    private Long bytesOut;

    /** Total bytesIn + bytesOut */
    private Long totalBytes;

    /** Momento de la medición */
    @Indexed
    private LocalDateTime timestamp;

    /**
     * Periodo de facturación en formato "YYYY-MM" (ej: "2026-08").
     * Facilita la agrupación en el aggregation pipeline sin
     * necesidad de operaciones $dateToString en runtime.
     */
    @Indexed
    private String billingPeriod;
}
