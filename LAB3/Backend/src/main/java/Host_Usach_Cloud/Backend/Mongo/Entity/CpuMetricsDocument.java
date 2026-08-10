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
@Document("cpu_metrics")
@CompoundIndex(name = "cpu_user_instance_idx", def = "{'userId': 1, 'instanceId': 1}")
public class CpuMetricsDocument {

    @Id
    private String id;

    @Indexed
    private Long userId;

    /** numericId de la instancia (sequential per-user) */
    @Indexed
    private Long instanceId;

    /** Porcentaje de uso de CPU calculado desde Docker stats (0.0 – 100.0+) */
    private Double cpuPercent;

    @Indexed
    private LocalDateTime timestamp;
}
