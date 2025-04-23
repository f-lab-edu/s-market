package org.sangyunpark.gateway.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class TokenProvider {

    private final String BEARER_PREFIX = "Bearer";

    private final SecretKey secretKey;

    public TokenProvider(@Value("${jwt.secret-key}") final String secretKey) {
        this.secretKey = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String resolveToken(final ServerHttpRequest request) {
        final String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if(!StringUtils.startsWith(authHeader, BEARER_PREFIX)) {
            return "";
        }

        return authHeader.substring(BEARER_PREFIX.length()).trim();
    }


    public Claims parseClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
