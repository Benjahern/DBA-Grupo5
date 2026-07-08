package Host_Usach_Cloud.Backend.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Genera y valida los JWT firmados con HS256. Es síncrono y CPU-puro;
 * los callers reactivos lo envuelven con {@code Mono.fromCallable(...)} cuando corresponda.
 *
 * Dos tipos de tokens se distinguen por el claim {@code type}:
 *   - {@code access}:   lleva email, name y roles; expira rápido (default 15 min).
 *   - {@code refresh}:  lleva solo el userId; expira largo (default 30 días).
 */
@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final String secret;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;
    private SecretKey signingKey;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-expiration:900000}") long accessExpirationMillis,
            @Value("${jwt.refresh-expiration:2592000000}") long refreshExpirationMillis
    ) {
        this.secret = secret;
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret no puede estar vacío");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret debe tener al menos 32 bytes (256 bits) para HS256; tiene " + keyBytes.length);
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId, String email, String name, Set<String> roles) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TYPE_ACCESS);
        claims.put("email", email);
        claims.put("name", name);
        claims.put("roles", roles);
        return buildToken(claims, String.valueOf(userId), now, accessExpirationMillis);
    }

    public String generateRefreshToken(Long userId) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TYPE_REFRESH);
        return buildToken(claims, String.valueOf(userId), now, refreshExpirationMillis);
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, Instant now, long expirationMillis) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Parsea el token, verifica firma y expiración, y exige {@code type == access}.
     * Lanza {@code io.jsonwebtoken.JwtException} (o subclase) si algo falla.
     */
    public Claims validateAccessToken(String token) {
        Claims claims = parseClaims(token);
        requireType(claims, TYPE_ACCESS);
        return claims;
    }

    /**
     * Igual a {@link #validateAccessToken(String)} pero exige {@code type == refresh}.
     */
    public Claims validateRefreshToken(String token) {
        Claims claims = parseClaims(token);
        requireType(claims, TYPE_REFRESH);
        return claims;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private static void requireType(Claims claims, String required) {
        Object type = claims.get("type");
        if (!(type instanceof String s) || !required.equals(s)) {
            throw new io.jsonwebtoken.JwtException("Token type inválido: se esperaba '" + required + "', se obtuvo '" + type + "'");
        }
    }

    /**
     * Helper para extraer roles como {@code List<String>} desde los claims.
     */
    @SuppressWarnings("unchecked")
    public static List<String> rolesOf(Claims claims) {
        Object raw = claims.get("roles");
        if (raw instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

    public long getAccessExpirationMillis() {
        return accessExpirationMillis;
    }

    public long getRefreshExpirationMillis() {
        return refreshExpirationMillis;
    }
}
