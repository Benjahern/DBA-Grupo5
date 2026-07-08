package Host_Usach_Cloud.Backend.Services;

import Host_Usach_Cloud.Backend.Entity.Role;
import Host_Usach_Cloud.Backend.Entity.User_role;
import Host_Usach_Cloud.Backend.Entity.Users;
import Host_Usach_Cloud.Backend.Repository.RoleRepository;
import Host_Usach_Cloud.Backend.Repository.UserRepository;
import Host_Usach_Cloud.Backend.Repository.UserRoleRepository;
import Host_Usach_Cloud.Backend.Security.JwtService;
import Host_Usach_Cloud.Backend.Services.DTO.AuthResult;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Set;

/**
 * Lógica de auth propia (registro / login / refresh).
 *
 * <p>BCrypt y JDBC se ejecutan en {@code Schedulers.boundedElastic()} para no bloquear
 * los event-loop de Netty — patrón ya presente en {@link UserService}.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TransactionTemplate transactionTemplate;

    public Mono<AuthResult> register(String email, String name, String rawPassword) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isPresent()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.CONFLICT,
                                "El email ya está registrado"));
                    }
                    return persistAndIssue(email, name, rawPassword, "user");
                });
    }

    public Mono<AuthResult> login(String email, String rawPassword) {
        return Mono.fromCallable(() -> userRepository.findByEmail(email))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isEmpty()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "Credenciales inválidas"));
                    }
                    Users u = opt.get();
                    boolean matches = passwordEncoder.matches(rawPassword, u.getPassword_hash());
                    if (!matches) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "Credenciales inválidas"));
                    }
                    if (u.isLock()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Cuenta bloqueada"));
                    }
                    return issueTokens(u);
                });
    }

    public Mono<AuthResult> refresh(String refreshTokenJwt) {
        return Mono.fromCallable(() -> {
                    Claims claims = jwtService.validateRefreshToken(refreshTokenJwt);
                    return Long.parseLong(claims.getSubject());
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::refreshForUserId);
    }

    private Mono<AuthResult> refreshForUserId(Long userId) {
        return Mono.fromCallable(() -> userRepository.findById(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(opt -> {
                    if (opt.isEmpty() || opt.get().isLock()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "Usuario no disponible"));
                    }
                    return issueTokens(opt.get());
                });
    }

    private Mono<AuthResult> persistAndIssue(String email, String name, String rawPassword, String roleName) {
        return Mono.fromCallable(() -> transactionTemplate.execute(status -> {
                    String hash = passwordEncoder.encode(rawPassword);
                    Users newUser = Users.builder()
                            .Email(email)
                            .Name(name)
                            .Max_instances(3)
                            .Lock(false)
                            .Password_hash(hash)
                            .build();
                    Users saved = userRepository.save(newUser);

                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                                    "Rol '" + roleName + "' no encontrado en BD"));

                    userRoleRepository.save(User_role.builder()
                            .User_id(saved.getUser_id())
                            .Role_id(role.getRole_id())
                            .build());
                    return saved;
                }))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(this::issueTokens);
    }

    private Mono<AuthResult> issueTokens(Users u) {
        return Mono.fromCallable(() -> {
                    Set<String> roles = userRoleRepository.findRoleNamesByUserId(u.getUser_id());
                    String access = jwtService.generateAccessToken(u.getUser_id(), u.getEmail(), u.getName(), roles);
                    String refresh = jwtService.generateRefreshToken(u.getUser_id());
                    return new AuthResult(u, access, refresh);
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
