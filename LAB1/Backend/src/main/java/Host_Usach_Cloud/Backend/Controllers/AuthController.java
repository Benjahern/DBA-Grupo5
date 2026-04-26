package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Controllers.DTO.LoginRequest;
import Host_Usach_Cloud.Backend.Controllers.DTO.RegisterRequest;
import Host_Usach_Cloud.Backend.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final WebClient.Builder webClientBuilder;

    @Value("${keycloak.server-url:http://keycloak:8080}")
    private String keycloakServerUrl;

    @Value("${keycloak.target-realm:host-usach}")
    private String realm;

    @Value("${keycloak.public-client-id:frontend-app}")
    private String clientId;

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

        return webClientBuilder.build()
                .post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("client_id", clientId)
                        .with("grant_type", "password")
                        .with("username", request.getEmail())
                        .with("password", request.getPassword()))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> ResponseEntity.ok((Object) response))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body((Object) Map.of("error", "Credenciales inválidas o error al contactar con Keycloak"))));
    }
}
