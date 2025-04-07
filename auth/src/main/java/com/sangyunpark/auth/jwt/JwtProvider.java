package com.sangyunpark.auth.jwt;


import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.constants.type.UserType;
import com.sangyunpark.auth.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtProvider {

    private final long accessTokenExpireTime;
    private final long refreshTokenExpireTime;
    private final Key secretKey;

    private static final String USER_TYPE = "userType";

    public JwtProvider
            (@Value("${jwt.secret-key}") final String secret,
             @Value("${jwt.accessToken-expiration}") final long accessTokenExpire,
             @Value("${jwt.refreshToken-expiration}") final long refreshTokenExpire) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpireTime = accessTokenExpire;
        this.refreshTokenExpireTime = refreshTokenExpire;
    }

    public String createAccessToken(final String email, final UserType userType) {
        return createToken(email, userType, accessTokenExpireTime);
    }

    public String createRefreshToken(final String email, final UserType userType) {
        return createToken(email, userType, refreshTokenExpireTime);
    }

    private String createToken(final String email, final UserType userType, final Long expireTime) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(Instant.now()))
                .claim(USER_TYPE, userType.name())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    public void validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        }catch (final JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String getEmail(final String token) {
        return parseClaim(token, Claims.SUBJECT, String.class);

    }

    public String getUserType(final String token) {
        return parseClaim(token, USER_TYPE, String.class);
    }

    private <T> T parseClaim(final String token, final String type, final Class<T> clazz) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get(type, clazz);
    }
}
