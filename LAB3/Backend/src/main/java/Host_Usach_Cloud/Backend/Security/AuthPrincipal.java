package Host_Usach_Cloud.Backend.Security;

import java.util.Set;

/**
 * Principal que se inyecta en el Authentication reactivo una vez validado el JWT.
 * Permite usar {@code @AuthenticationPrincipal AuthPrincipal p} en los controllers.
 */
public record AuthPrincipal(Long userId, String email, String name, Set<String> roles) {}
