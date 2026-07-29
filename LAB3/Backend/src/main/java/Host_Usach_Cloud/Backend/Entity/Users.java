package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Users {

    private Long User_id;

    private String Email;

    private String Name;

    private int Max_instances;

    private boolean Lock;

    private String Password_hash;

}
