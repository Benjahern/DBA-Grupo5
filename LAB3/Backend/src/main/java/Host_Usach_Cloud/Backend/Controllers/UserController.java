package Host_Usach_Cloud.Backend.Controllers;

import Host_Usach_Cloud.Backend.Entity.Users;
import Host_Usach_Cloud.Backend.Repository.UserRepository;
import Host_Usach_Cloud.Backend.Security.AuthPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public Mono<ResponseEntity<Object>> getCurrentUser(@AuthenticationPrincipal AuthPrincipal principal) {
        if (principal == null) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return Mono.fromCallable(() -> userRepository.findById(principal.userId()))
                .map(opt -> opt
                        .<ResponseEntity<Object>>map(u -> ResponseEntity.ok((Object) sanitize(u)))
                        .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body((Object) Map.of("error", "User not found in DB"))));
    }

    private static Map<String, Object> sanitize(Users u) {
        return Map.of(
                "User_id", u.getUser_id(),
                "Email", u.getEmail(),
                "Name", u.getName(),
                "Max_instances", u.getMax_instances(),
                "Lock", u.isLock()
        );
    }
}
