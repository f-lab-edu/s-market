package org.sangyunpark.gateway;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("NonAsciiCharacters")
class TokenValidatorTest {

    private static final String SECRET_KEY = "test-secret-key-test-secret-key-test-secret-key";
    private static final String ANOTHER_SECRET_KEY = "another-secret-key-another-secret-key";
    private static final String INVALID_TOKEN = "invalid-token";

    private TokenValidator tokenValidator;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        // 256bit 이상
        tokenValidator = new TokenValidator(SECRET_KEY);
        signingKey = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    @Test
    @DisplayName("유효한 토큰은 true 반환해야 한다")
    void 유효한_토큰은_true_반환() {
        String validToken = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 1000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        boolean result = tokenValidator.validateToken(validToken);

        assertTrue(result);
    }

    @Test
    @DisplayName("만료된 토큰은 false 반환해야 한다")
    void 만료된_토큰은_false_반환() {
        String expiredToken = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date(System.currentTimeMillis() - 60000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        boolean result = tokenValidator.validateToken(expiredToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("잘못된 서명의 토큰은 false 반환해야 한다")
    void 잘못된_서명의_토큰은_false_반환() {
        Key otherKey = Keys.hmacShaKeyFor(ANOTHER_SECRET_KEY.getBytes());
        String invalidToken = Jwts.builder()
                .setSubject("user1")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey, SignatureAlgorithm.HS256)
                .compact();

        boolean result = tokenValidator.validateToken(invalidToken);

        assertFalse(result);
    }

    @Test
    @DisplayName("형식이 잘못된 토큰은 false 반환해야 한다")
    void 형식이_잘못된_토큰은_false_반환() {
        boolean result = tokenValidator.validateToken(INVALID_TOKEN);
        assertFalse(result);
    }
}