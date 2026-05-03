package Host_Usach_Cloud.Backend.Entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Region {

    @JsonProperty("region_id")
    private Long Region_id;

    @JsonProperty("Name")
    private String Name;
}
