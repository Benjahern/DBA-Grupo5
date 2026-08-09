package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "consumption")
public class Consumption {

    @Id
    private Long Consumption_id;

    private Double Cpu_stats;

    private Double Ram_stats;

    private Double Storage_stats;

    /**
     * FK a {@link Host_Usach_Cloud.Backend.Mongo.Entity.InstanceDocument#getNumericId()}.
     * Bigint para coincidir con la columna Postgres.Instance_id (BIGINT).
     */
    private Long Instance_id;

    @Indexed(expireAfterSeconds = 2592000)
    private LocalDateTime Created_at;
}
