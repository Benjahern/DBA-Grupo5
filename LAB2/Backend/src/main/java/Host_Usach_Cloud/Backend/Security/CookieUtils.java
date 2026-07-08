package Host_Usach_Cloud.Backend.Security;

import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.time.Duration;

public final class CookieUtils {
    private CookieUtils() {}

    public static String extractCookie(ServerHttpRequest request, String name) {
        var cookies = request.getCookies();
        if (cookies == null) return null;
        var cookie = cookies.getFirst(name);
        return cookie != null ? cookie.getValue() : null;
    }

    public static ResponseCookie buildAuthCookie(String name, String value, long maxAgeMillis, boolean secure, String sameSite) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite(sameSite)
                .maxAge(Duration.ofMillis(maxAgeMillis))
                .build();
    }

    public static ResponseCookie buildClearCookie(String name, boolean secure) {
        return ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
