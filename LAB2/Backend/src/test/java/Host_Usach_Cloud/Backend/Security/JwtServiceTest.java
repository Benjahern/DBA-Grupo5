package Host_Usach_Cloud.Backend.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-with-at-least-32-bytes-of-length-aa";
    private JwtService jwt;

    @BeforeEach
    void setUp() {
        jwt = new JwtService(SECRET, 60_000L, 1_200_000L);
        // Llama al @PostConstruct manualmente.
        try {
            var m = JwtService.class.getDeclaredMethod("init");
            m.setAccessible(true);
            m.invoke(jwt);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void accessTokenHasAccessTypeAndUserClaims() {
        String token = jwt.generateAccessToken(42L, "alice@example.com", "Alice", Set.of("user"));

        Claims claims = jwt.validateAccessToken(token);

        assertThat(claims.getSubject()).isEqualTo("42");
        assertThat(claims.get("type", String.class)).isEqualTo(JwtService.TYPE_ACCESS);
        assertThat(claims.get("email", String.class)).isEqualTo("alice@example.com");
        assertThat(claims.get("name", String.class)).isEqualTo("Alice");
        assertThat(claims.get("roles")).asList().containsExactly("user");
    }

    @Test
    void refreshTokenIsRejectedByAccessValidator() {
        String refresh = jwt.generateRefreshToken(42L);

        assertThatThrownBy(() -> jwt.validateAccessToken(refresh))
                .isInstanceOf(io.jsonwebtoken.JwtException.class)
                .hasMessageContaining("Token type inválido");
    }

    @Test
    void accessTokenIsRejectedByRefreshValidator() {
        String access = jwt.generateAccessToken(42L, "alice@example.com", "Alice", Set.of("user"));

        assertThatThrownBy(() -> jwt.validateRefreshToken(access))
                .isInstanceOf(io.jsonwebtoken.JwtException.class)
                .hasMessageContaining("Token type inválido");
    }

    @Test
    void tamperedSignatureIsRejected() {
        String token = jwt.generateAccessToken(42L, "alice@example.com", "Alice", Set.of("user"));
        // Cambiamos los últimos 5 caracteres de la firma.
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThatThrownBy(() -> jwt.validateAccessToken(tampered))
                .isInstanceOf(SignatureException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        JwtService shortJwt = new JwtService(SECRET, -1000L, -1L); // ya expirado
        // Forzar init sin @PostConstruct.
        try {
            var m = JwtService.class.getDeclaredMethod("init");
            m.setAccessible(true);
            m.invoke(shortJwt);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        String token = shortJwt.generateAccessToken(42L, "alice@example.com", "Alice", Set.of("user"));

        assertThatThrownBy(() -> jwt.validateAccessToken(token))
                .isInstanceOf(ExpiredJwtException.class);
    }
}
