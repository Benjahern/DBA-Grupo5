package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Storage {

    private Long Storage_id;

    private int Quantity;

    // Coste por hora
    private float Cost_ph;
}
