package Host_Usach_Cloud.Backend.Config;

import Host_Usach_Cloud.Backend.Security.JsonAccessDeniedHandler;
import Host_Usach_Cloud.Backend.Security.JsonAuthenticationEntryPoint;
import Host_Usach_Cloud.Backend.Security.JwtAuthenticationWebFilter;
import Host_Usach_Cloud.Backend.Security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${cors.allowed-headers:Authorization,Content-Type}")
    private String allowedHeaders;

    @Value("${cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * El filtro NO es @Component: lo instanciamos nosotros para evitar doble registro.
     */
    @Bean
    public JwtAuthenticationWebFilter jwtAuthenticationWebFilter(JwtService jwtService) {
        return new JwtAuthenticationWebFilter(jwtService);
    }

    @Bean
    public JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint() {
        return new JsonAuthenticationEntryPoint();
    }

    @Bean
    public JsonAccessDeniedHandler jsonAccessDeniedHandler() {
        return new JsonAccessDeniedHandler();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            JwtAuthenticationWebFilter jwtAuthenticationWebFilter,
            JsonAuthenticationEntryPoint entryPoint,
            JsonAccessDeniedHandler accessDeniedHandler
    ) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                .pathMatchers("/api/auth/**").permitAll()
                    .pathMatchers(HttpMethod.GET, "/api/risks").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/datacenters/**").permitAll()
                .anyExchange().authenticated()
            )
            .addFilterAt(jwtAuthenticationWebFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling(handler -> handler
                .authenticationEntryPoint(entryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
