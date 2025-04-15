package org.sangyunpark.gateway.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sangyunpark.gateway.filter.dto.CachedUser;
import org.sangyunpark.gateway.infrastructure.redis.RedisTokenRepository;
import org.sangyunpark.gateway.jwt.TokenProvider;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private RedisTokenRepository redisTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("캐시에 사용자 정보가 있는 경우 바로 캐싱된 값을 리턴합니다.")
    void 캐시에_사용자_정보가_있으면_바로_리턴() throws Exception {
        CachedUser cachedUser = new CachedUser("user@example.com", "NORMAL", "ACTIVE");
        String json = "{\"email\":\"user@example.com\",\"userType\":\"NORMAL\",\"userStatus\":\"ACTIVE\"}";

        when(redisTokenRepository.isBlackList("token")).thenReturn(Mono.just(false));
        when(redisTokenRepository.getCachedUser("token")).thenReturn(Mono.just(json));
        when(objectMapper.readValue(json, CachedUser.class)).thenReturn(cachedUser);

        StepVerifier.create(authenticationService.getAuthenticatedUser("token"))
                .expectNextMatches(user -> user.email().equals("user@example.com"))
                .verifyComplete();

        verify(redisTokenRepository).isBlackList("token");
    }

    @Test
    @DisplayName("캐시에 사용자 정보가 없으면 JWT 파싱 후 캐싱하고 리턴")
    void 캐시_미스_시_파싱하고_캐싱() throws Exception {
        // given
        Claims claims = Jwts.claims();
        claims.setSubject("user@example.com");
        claims.put("userType", "NORMAL");
        claims.put("userStatus", "ACTIVE");

        CachedUser expectedUser = new CachedUser("user@example.com", "NORMAL", "ACTIVE");
        String json = objectMapper.writeValueAsString(expectedUser);

        when(redisTokenRepository.getCachedUser("token")).thenReturn(Mono.empty());
        when(redisTokenRepository.isBlackList("token")).thenReturn(Mono.just(false));
        when(tokenProvider.parseClaims("token")).thenReturn(claims);
        when(objectMapper.writeValueAsString(expectedUser)).thenReturn(json);
        when(redisTokenRepository.saveAuthToken("auth_token:token", json, Duration.ofMinutes(10))).thenReturn(Mono.just(true));

        // when + then
        StepVerifier.create(authenticationService.getAuthenticatedUser("token"))
                .expectNext(expectedUser)
                .verifyComplete();
    }

    @Test
    @DisplayName("블랙리스트 토큰이면 AuthenticatedUser에서 오류 발생")
    void 블랙리스트_토큰_예외() {
        // given
        when(redisTokenRepository.isBlackList("token")).thenReturn(Mono.just(true));

        // when + then
        StepVerifier.create(authenticationService.getAuthenticatedUser("token"))
                .expectError(RuntimeException.class)
                .verify();

        // ✅ 캐시 조회는 시도하지 않았는지 검증
        verify(redisTokenRepository, never()).getCachedUser(any());
    }

    @Test
    @DisplayName("JWT에서 Claims 파싱 후 CachedUser 객체 생성")
    void parseClaims_정상작동() {
        Claims claims = Jwts.claims();
        claims.setSubject("user@example.com");
        claims.put("userType", "NORMAL");
        claims.put("userStatus", "ACTIVE");

        when(tokenProvider.parseClaims("token")).thenReturn(claims);

        StepVerifier.create(authenticationService.parseClaims("token"))
                .expectNextMatches(user ->
                        user.email().equals("user@example.com") &&
                                user.userType().equals("NORMAL") &&
                                user.userStatus().equals("ACTIVE"))
                .verifyComplete();
    }

    @Test
    @DisplayName("캐시 저장이 성공하면 true 반환")
    void cacheUser_성공() throws Exception {
        CachedUser user = new CachedUser("user@example.com", "NORMAL", "ACTIVE");
        String json = objectMapper.writeValueAsString(user);

        when(objectMapper.writeValueAsString(user)).thenReturn(json);
        when(redisTokenRepository.saveAuthToken("auth_token:token", json, Duration.ofMinutes(10))).thenReturn(Mono.just(true));

        StepVerifier.create(authenticationService.cacheUser("token", user))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Redis에서 잘못된 JSON 반환 시 예외 발생")
    void getCachedUser_JSON파싱실패() throws Exception {
        String invalidJson = "{invalid_json}";

        when(redisTokenRepository.getCachedUser("token")).thenReturn(Mono.just(invalidJson));
        when(objectMapper.readValue(invalidJson, CachedUser.class)).thenThrow(new RuntimeException("파싱 오류"));

        StepVerifier.create(authenticationService.getCachedUser("token"))
                .expectError(RuntimeException.class)
                .verify();
    }
}