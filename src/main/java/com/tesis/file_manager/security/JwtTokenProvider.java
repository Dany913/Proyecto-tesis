package com.tesis.file_manager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.Base64;
import javax.crypto.SecretKey;

@Component
public class JwtTokenProvider {

    private final SecretKey jwtSecret = Keys.hmacShaKeyFor(Base64.getDecoder().decode(
            Base64.getEncoder().encodeToString("claveSuperSeguraParaJWT1234567890".getBytes())
    ));

    private final long jwtExpirationMs = 31536000000L; //

     //🔹 Generar token
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(jwtSecret)
                .compact();
    }

     //🔹 Obtener username desde el token
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(jwtSecret)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

     //🔹 Validar token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecret).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("Token inválido: " + e.getMessage());
            return false;
        }
    }
}

