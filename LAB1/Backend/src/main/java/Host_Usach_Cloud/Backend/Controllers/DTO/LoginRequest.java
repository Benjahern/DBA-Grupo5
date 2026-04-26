package Host_Usach_Cloud.Backend.Controllers.DTO;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
}
