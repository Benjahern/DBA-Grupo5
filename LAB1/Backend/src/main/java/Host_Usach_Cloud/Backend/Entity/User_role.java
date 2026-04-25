package Host_Usach_Cloud.Backend.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User_role {

    private Long User_role_id;

    private Long User_id;

    private Long Role_id;
}
