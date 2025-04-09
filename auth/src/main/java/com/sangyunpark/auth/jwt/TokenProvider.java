package com.sangyunpark.auth.jwt;


import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
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
public class TokenProvider {

    private final long accessTokenExpireTime;
    private final long refreshTokenExpireTime;
    private final Key secretKey;

    private static final String USER_TYPE = "userType";
    private static final String USER_STATUS = "userStatus";

    public TokenProvider
            (@Value("${jwt.secret-key}") final String secret,
             @Value("${jwt.accessToken-expiration}") final long accessTokenExpire,
             @Value("${jwt.refreshToken-expiration}") final long refreshTokenExpire) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpireTime = accessTokenExpire;
        this.refreshTokenExpireTime = refreshTokenExpire;
    }

    public String createAccessToken(final String email, final UserType userType, final UserStatus userStatus) {
        return createToken(email, userType, userStatus ,accessTokenExpireTime);
    }

    public String createRefreshToken(final String email, final UserType userType, final UserStatus userStatus) {
        return createToken(email, userType, userStatus ,refreshTokenExpireTime);
    }

    private String createToken(final String email, final UserType userType, final UserStatus userStatus, final Long expireTime) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(Date.from(Instant.now()))
                .claim(USER_TYPE, userType.name())
                .claim(USER_STATUS, userStatus.name())
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);

        }catch (final JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        return true;
    }

    public String getEmail(final String token) {
        return parseClaims(token).getSubject();

    }

    public String getUserType(final String token) {
        return parseClaims(token).get(USER_TYPE, String.class);
    }

    public long getRemainingExpiration(String accessToken) {
        Date expiration = parseClaims(accessToken).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }

    private Claims parseClaims(final String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
