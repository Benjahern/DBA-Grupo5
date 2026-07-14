package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PingResult {
    private Long   region_id;
    private String region_name;
    private Double distance_m;
    private Double latency_rtt_ms;
}
