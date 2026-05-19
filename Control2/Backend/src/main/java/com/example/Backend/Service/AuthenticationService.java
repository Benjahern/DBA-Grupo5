package com.example.Backend.Service;

import com.example.Backend.Entity.Dtos.AuthResponse;
import com.example.Backend.Entity.Dtos.LoginRequest;
import com.example.Backend.Entity.Dtos.RegisterRequest;
import com.example.Backend.Entity.UserEntity;
import com.example.Backend.Repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthenticationService {

    private static final String AUTH_COOKIE_NAME = "auth_token";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                 JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;

    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByUserName(request.getUsername()) != null) {
            throw new RuntimeException("El usuario ya existe");
        }

        UserEntity user = new UserEntity();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        //para cumplir con el requisito de ingresar su direccion geografica
        GeometryFactory geometryFactory = new GeometryFactory();
        Coordinate coordinate = new Coordinate(request.getLongitude(), request.getLatitude());
        Point point = geometryFactory.createPoint(coordinate);
        user.setGeoLocation(point);

        UserEntity savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(token, savedUser.getUserName());
    }

    public AuthResponse login(LoginRequest request) {
        UserEntity user = userRepository.findByUserName(request.getUsername());
        if (user == null) {
            user = userRepository.findByEmail(request.getUsername());
        }
        if (user == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password incorrecto");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, user.getUserName());
    }

    public ResponseCookie buildAuthCookie(String token) {
        return ResponseCookie.from(AUTH_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ofMillis(jwtExpiration))
                .build();
    }

    public ResponseCookie expireAuthCookie() {
        return ResponseCookie.from(AUTH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .build();
    }


}
