package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Controllers.DTO.LoginRequest;
import Host_Usach_Cloud.Backend.Controllers.DTO.RegisterRequest;
import Host_Usach_Cloud.Backend.Services.UserService;
import Host_Usach_Cloud.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
        private final UserRepository userRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${keycloak.server-url:http://keycloak:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.target-realm:host-usach}")
    private String realm;

        @Value("${keycloak.backend-client-id:backend-app}")
        private String backendClientId;

        @Value("${keycloak.backend-client-secret:CHANGE_ME_BACKEND_CLIENT_SECRET}")
        private String backendClientSecret;

    @PostMapping("/register")
        public Mono<ResponseEntity<Object>> register(@RequestBody RegisterRequest request) {
        return userService.createUser(request.getEmail(), request.getName(), request.getPassword())
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body((Object) user))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body((Object) Map.of("error", e.getMessage()))));
    }

    @PostMapping("/login")
        public Mono<ResponseEntity<Object>> login(@RequestBody LoginRequest request) {
        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

                var formData = BodyInserters.fromFormData("client_id", backendClientId)
                        .with("grant_type", "password")
                        .with("username", request.getEmail())
                        .with("password", request.getPassword());

                if (StringUtils.hasText(backendClientSecret)) {
                    formData = formData.with("client_secret", backendClientSecret);
                }

        return webClientBuilder.build()
                .post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .body(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> body = new HashMap<>(response);
                    userRepository.findByEmail(request.getEmail())
                            .ifPresent(user -> body.put("user", user));
                    return ResponseEntity.ok((Object) body);
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body((Object) Map.of("error", "Credenciales inválidas o error al contactar con Keycloak"))));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Object>> refresh(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refresh_token");
        if (!StringUtils.hasText(refreshToken)) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body((Object) Map.of("error", "Refresh token es requerido")));
        }

        String tokenUrl = keycloakServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";

        var formData = BodyInserters.fromFormData("client_id", backendClientId)
                .with("grant_type", "refresh_token")
                .with("refresh_token", refreshToken);

        if (StringUtils.hasText(backendClientSecret)) {
            formData = formData.with("client_secret", backendClientSecret);
        }

        return webClientBuilder.build()
                .post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ResponseEntity.ok((Object) response))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body((Object) Map.of("error", "Token inválido o expirado"))));
    }
}
