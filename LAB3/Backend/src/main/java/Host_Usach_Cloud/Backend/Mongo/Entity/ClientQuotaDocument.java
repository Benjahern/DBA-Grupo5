package Host_Usach_Cloud.Backend.Mongo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("client_quotas")
public class ClientQuotaDocument {

    @Id
    private Long userId;

    private Integer maxInstances;

    private Integer activeCount;

    /** Límite de ancho de banda mensual del plan, en bytes. Null = sin límite definido. */
    private Long maxBandwidthBytes;
}