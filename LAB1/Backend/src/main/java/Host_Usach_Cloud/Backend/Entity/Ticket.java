package Host_Usach_Cloud.Backend.Entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Ticket {

    private Long Ticket_id;

    private Long Instance_id;

    // El uso de la instancia medido en tiempo
    private Duration Usage;

    private float Price;
}
