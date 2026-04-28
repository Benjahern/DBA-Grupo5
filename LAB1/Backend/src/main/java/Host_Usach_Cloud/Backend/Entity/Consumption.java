package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Consumption {

    private Long Consumption_id;

    private Double Cpu_stats;

    private Double Ram_stats;

    private Double Storage_stats;

    private Long Instance_id;

    private LocalDateTime Created_at;
}
