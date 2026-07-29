package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Controllers.DTO.LoginRequest;
import Host_Usach_Cloud.Backend.Controllers.DTO.RegisterRequest;
import Host_Usach_Cloud.Backend.Entity.Users;
import Host_Usach_Cloud.Backend.Repository.UserRoleRepository;
import Host_Usach_Cloud.Backend.Security.CookieNames;
import Host_Usach_Cloud.Backend.Security.CookieUtils;
import Host_Usach_Cloud.Backend.Security.JwtService;
import Host_Usach_Cloud.Backend.Services.AuthService;
import Host_Usach_Cloud.Backend.Services.DTO.AuthResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRoleRepository userRoleRepository;

    @Value("${jwt.cookie-secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.cookie-same-site:Lax}")
    private String cookieSameSite;

    @PostMapping("/register")
    public Mono<ResponseEntity<Map<String, Object>>> register(
            @RequestBody RegisterRequest request,
            ServerHttpResponse response) {
        return authService.register(request.getEmail(), request.getName(), request.getPassword())
                .flatMap(result -> buildAuthResponse(result, response))
                .onErrorResume(this::toErrorResponse);
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, Object>>> login(
            @RequestBody LoginRequest request,
            ServerHttpResponse response) {
        return authService.login(request.getEmail(), request.getPassword())
                .flatMap(result -> buildAuthResponse(result, response))
                .onErrorResume(this::toErrorResponse);
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, Object>>> refresh(
            ServerHttpRequest request,
            ServerHttpResponse response) {
        String refreshToken = CookieUtils.extractCookie(request, CookieNames.REFRESH_COOKIE);
        if (refreshToken == null || refreshToken.isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Refresh token no presente")));
        }
        return authService.refresh(refreshToken)
                .flatMap(result -> buildAuthResponse(result, response))
                .onErrorResume(this::toErrorResponse);
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, Object>>> logout(ServerHttpResponse response) {
        response.addCookie(CookieUtils.buildClearCookie(CookieNames.AUTH_COOKIE, cookieSecure));
        response.addCookie(CookieUtils.buildClearCookie(CookieNames.REFRESH_COOKIE, cookieSecure));
        return Mono.just(ResponseEntity.ok(Map.of("message", "Logged out")));
    }

    private Mono<ResponseEntity<Map<String, Object>>> buildAuthResponse(AuthResult result, ServerHttpResponse response) {
        Users u = result.user();
        ResponseCookie authCookie = CookieUtils.buildAuthCookie(
                CookieNames.AUTH_COOKIE,
                result.accessToken(),
                jwtService.getAccessExpirationMillis(),
                cookieSecure,
                cookieSameSite
        );
        ResponseCookie refreshCookie = CookieUtils.buildAuthCookie(
                CookieNames.REFRESH_COOKIE,
                result.refreshToken(),
                jwtService.getRefreshExpirationMillis(),
                cookieSecure,
                "Strict"
        );
        response.addCookie(authCookie);
        response.addCookie(refreshCookie);

        // Roles se consultan aparte (en JDBC boundedElastic) para no bloquear el event-loop.
        return Mono.fromCallable(() -> userRoleRepository.findRoleNamesByUserId(u.getUser_id()))
                .subscribeOn(Schedulers.boundedElastic())
                .map(roles -> {
                    // No exponemos Password_hash al frontend.
                    Map<String, Object> userBody = new HashMap<>();
                    userBody.put("User_id", u.getUser_id());
                    userBody.put("Email", u.getEmail());
                    userBody.put("Name", u.getName());
                    userBody.put("Max_instances", u.getMax_instances());
                    userBody.put("Lock", u.isLock());
                    userBody.put("Roles", roles);
                    userBody.put("Sub", String.valueOf(u.getUser_id())); // compatibilidad con código Keycloak-era
                    return ResponseEntity.ok(Map.of("user", userBody));
                });
    }

    private Mono<ResponseEntity<Map<String, Object>>> toErrorResponse(Throwable e) {
        if (e instanceof ResponseStatusException rse) {
            String message = rse.getReason() != null ? rse.getReason() : "Error";
            return Mono.just(ResponseEntity.status(rse.getStatusCode())
                    .body(Map.of("error", message)));
        }
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno")));
    }
}