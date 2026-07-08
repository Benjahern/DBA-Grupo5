package Host_Usach_Cloud.Backend.Security;

import Host_Usach_Cloud.Backend.Entity.Users;
import Host_Usach_Cloud.Backend.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Optional;

/**
 * Garantiza que el usuario administrador definido en {@code createDb.sql} tenga una
 * password BCrypt válida. Idempotente: si ya tiene hash, no toca nada.
 *
 * <p>Lee email y password inicial de variables de entorno (con defaults dev). En CI
 * o producción se deben sobreescribir {@code ADMIN_EMAIL} y {@code ADMIN_INITIAL_PASSWORD}.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.admin-email:admin@gmail.com}")
    private String adminEmail;

    @Value("${jwt.admin-initial-password:Admin123!}")
    private String adminInitialPassword;

    @EventListener(ApplicationReadyEvent.class)
    public void seedAdmin() {
        Mono.fromCallable(() -> {
                    Optional<Users> opt = userRepository.findByEmail(adminEmail);
                    if (opt.isEmpty()) {
                        log.warn("PasswordSeeder: usuario '{}' no existe aún — saltando", adminEmail);
                        return false;
                    }
                    Users u = opt.get();
                    if (u.getPassword_hash() != null && !u.getPassword_hash().isBlank()) {
                        log.info("PasswordSeeder: usuario '{}' ya tiene Password_hash, saltando", adminEmail);
                        return false;
                    }
                    String hash = passwordEncoder.encode(adminInitialPassword);
                    boolean updated = userRepository.updatePasswordHash(u.getUser_id(), hash);
                    if (updated) {
                        log.info("PasswordSeeder: Password_hash seteado para '{}'", adminEmail);
                    }
                    return updated;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        ok -> {},
                        err -> log.error("PasswordSeeder falló: {}", err.getMessage(), err)
                );
    }
}
