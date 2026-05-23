package com.example.Backend.Controller;

import com.example.Backend.Entity.Dtos.AuthResponse;
import com.example.Backend.Entity.Dtos.LoginRequest;
import com.example.Backend.Entity.Dtos.RegisterRequest;
import com.example.Backend.Service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Autowired
    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.register(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authenticationService.buildAuthCookie(response.getToken()).toString())
            .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, authenticationService.buildAuthCookie(response.getToken()).toString())
            .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of("username", authentication.getName()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authenticationService.expireAuthCookie().toString())
                .build();
    }
}
