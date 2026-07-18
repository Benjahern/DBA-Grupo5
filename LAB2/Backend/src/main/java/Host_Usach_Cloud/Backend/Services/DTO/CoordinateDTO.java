package Host_Usach_Cloud.Backend.Services.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoordinateDTO {

    private Double latitude;
    private Double longitude;
}