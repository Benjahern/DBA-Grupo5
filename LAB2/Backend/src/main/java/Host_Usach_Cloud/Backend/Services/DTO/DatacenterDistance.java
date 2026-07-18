package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DatacenterDistance {

    private Long datacenterId;

    private String name;

    private Double latitude;

    private Double longitude;

    private Long riskZoneId;

    private Double distanceKm;
}
