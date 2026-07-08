package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConsumptionProjection {

    private double projectedCpu;
    private double projectedRam;
    private double projectedStorage;

    private int samplesSoFar;
    private int samplesProjected;
}
