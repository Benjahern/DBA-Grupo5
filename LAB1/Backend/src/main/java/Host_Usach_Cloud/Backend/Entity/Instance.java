package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Instance {

    private Long Instance_id;

    private String Name;

    private Long Ram_id;

    private Long Cpu_id;

    private LocalDateTime Started_at;

    private Long Storage_id;

    private boolean Terminated;

    //Obligado a ser "Running" "Stopped" "Terminated"
    private String State;

    private Long User_id;

    private Long Region_id;

    // Container de Docker
    private String Container_id;

    private Duration Active_hours;

    private String Ip_address;

    //parametro pedido por el marco
    private String Color;

    //parametro sando
    private String Base_image;
}
