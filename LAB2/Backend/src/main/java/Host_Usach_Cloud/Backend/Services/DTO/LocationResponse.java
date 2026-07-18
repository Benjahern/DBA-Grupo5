package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponse {

    private Long regionId;

    private String regionName;

    private Long riskZoneId;

    private String riskZoneName;

}