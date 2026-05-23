package com.example.Backend.Service;

import com.example.Backend.Entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {


    // esto debe ir en las variables de entorno
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // este llama al de abajo, ya que primero creamos el claim con el maphash esto es para almacenarlo en el token
    public String generateToken(UserEntity userEntity) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userEntity.getId());
        extraClaims.put("email", userEntity.getEmail());
        return generateToken(extraClaims, userEntity);
    }


    //Genera el token copn el tiemppo
    public String generateToken(Map<String, Object> extraClaims, UserEntity userEntity) {
        return buildToken(extraClaims, userEntity, jwtExpiration);
    }

    // Aca ya se crea el hash completo con la firma
    private String buildToken(Map<String, Object> extraClaims, UserEntity userEntity, long expiration) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userEntity.getUserName())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
