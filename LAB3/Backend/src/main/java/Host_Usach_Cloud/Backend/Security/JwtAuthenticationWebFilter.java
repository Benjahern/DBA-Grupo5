package Host_Usach_Cloud.Backend.Security;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro reactivo que reemplaza el resource server de Keycloak.
 *
 * <p>Lee el JWT (cookie {@code auth_token} primero; header {@code Authorization: Bearer} como fallback),
 * valida que sea un access token, y propaga un {@link UsernamePasswordAuthenticationToken} en el
 * contexto de seguridad reactivo.</p>
 *
 * <p>NO es {@code @Component}: se instancia como @Bean en SecurityConfig para tener control sobre
 * el orden (vía {@code addFilterAt(SecurityWebFiltersOrder.AUTHENTICATION)}).</p>
 *
 * <p>Ante token ausente o inválido, NO escribe respuesta — deja que SecurityConfig + EntryPoint
 * se encarguen del 401. Evita la doble escritura (filter + entry point).</p>
 */
public class JwtAuthenticationWebFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationWebFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange);
        if (token == null) {
            return chain.filter(exchange);
        }
        return Mono.fromCallable(() -> jwtService.validateAccessToken(token))
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::buildAuthentication)
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .onErrorResume(e -> chain.filter(exchange));
    }

    private String extractToken(ServerWebExchange exchange) {
        String cookieToken = CookieUtils.extractCookie(exchange.getRequest(), CookieNames.AUTH_COOKIE);
        if (cookieToken != null && !cookieToken.isBlank()) {
            return cookieToken;
        }
        String header = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String value = header.substring(BEARER_PREFIX.length()).trim();
            if (!value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Authentication buildAuthentication(Claims claims) {
        Long userId = Long.parseLong(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        @SuppressWarnings("unchecked")
        List<String> rolesRaw = (List<String>) claims.get("roles");
        Set<String> roles = rolesRaw == null ? Set.of() : Set.copyOf(rolesRaw);

        AuthPrincipal principal = new AuthPrincipal(userId, email, name, roles);

        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(principal, null, authorities);
    }
}
