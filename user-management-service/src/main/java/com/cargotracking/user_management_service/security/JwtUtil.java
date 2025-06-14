package com.cargotracking.user_management_service.security;

import com.cargotracking.user_management_service.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT Utility class - Requirements NFR-SEC-002
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    private final long expiration = 86400000; // 1 gün

    /**
     * Secret key oluşturur - minimum 256 bit gerekli
     * JWT HMAC-SHA algoritmaları için güvenli key oluşturur
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // Key'in minimum 32 byte (256 bit) olduğunu kontrol et
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                "JWT secret key must be at least 256 bits (32 characters) long. " +
                "Current key length: " + keyBytes.length + " bytes"
            );
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String username, Set<Role> roles) {
        String rolesString = roles.stream()
                .map(Role::name)
                .collect(Collectors.joining(","));
                
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", rolesString)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }
}
