package Host_Usach_Cloud.Backend.Controllers.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String email;
    private String name;
    private String password;
}
