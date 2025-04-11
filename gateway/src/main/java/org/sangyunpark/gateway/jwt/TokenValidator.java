package org.sangyunpark.gateway.jwt;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
public class TokenValidator {

    private final Key secretKey;

    public TokenValidator(@Value("${jwt.secret-key}") final String secret) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }

        return true;
    }
}
