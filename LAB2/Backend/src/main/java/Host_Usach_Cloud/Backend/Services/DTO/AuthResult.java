package Host_Usach_Cloud.Backend.Services.DTO;

import Host_Usach_Cloud.Backend.Entity.Users;

/**
 * Estructura interna usada por {@link Host_Usach_Cloud.Backend.Services.AuthService} y
 * {@link Host_Usach_Cloud.Backend.Controllers.AuthController}. NO se devuelve por JSON
 * porque los tokens viajan solo en cookies HttpOnly.
 */
public record AuthResult(Users user, String accessToken, String refreshToken) {}
