package com.sangyunpark.auth.jwt;

import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@SuppressWarnings("NonAsciiCharacters")
class TokenProviderTest {

    private static final String SECRET = "qwieuqwoieqwoieuqwoieuqwoeowqieuwqioeuwqoeiqwueoqwieuqwoieuqwoie"; // 최소 256bit 이상
    private static final long ACCESS_TOKEN_EXPIRE = 1000 * 2;  // 2초
    private static final long REFRESH_TOKEN_EXPIRE = 1000 * 60 * 60; // 1시간

    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new TokenProvider(SECRET, ACCESS_TOKEN_EXPIRE, REFRESH_TOKEN_EXPIRE);
    }

    @Test
    @DisplayName("빈 토큰이면 예외를 던진다.")
    void 빈_토큰_예외() {
        assertThatThrownBy(() -> tokenProvider.validateToken(""))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("null 토큰이면 예외가 발생한다.")
    void null_토큰_예외() {
        assertThatThrownBy(() -> tokenProvider.validateToken(null))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("정상적인 형식이 아닌 문자열이 토큰이면 예외가 발생한다.")
    void 잘못된_형식의_토큰_예외() {
        String malformedToken = "this-is-not-a-valid-jwt";

        assertThatThrownBy(() -> tokenProvider.validateToken(malformedToken))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("액세스 토큰 생성 및 검증 성공")
    void 액세스_토큰_생성_및_검증_성공() {
        // given
        String email = "test@example.com";
        UserType userType = UserType.NORMAL;

        // when
        String accessToken = tokenProvider.createAccessToken(email, userType);

        // then
        assertThat(accessToken).isNotBlank();
        assertThatCode(() -> tokenProvider.validateToken(accessToken)).doesNotThrowAnyException();

        assertThat(tokenProvider.getEmail(accessToken)).isEqualTo(email);
        assertThat(tokenProvider.getUserType(accessToken)).isEqualTo(userType.name());
    }

    @Test
    @DisplayName("만료된 토큰이면 예외가 발생한다.")
    void 만료된_토큰이면_예외가_발생한다() throws InterruptedException {
        // given
        TokenProvider shortLivedProvider = new TokenProvider(
                "your-256-bit-secret-key-your-256-bit-secret-key",
                1, // access token 1ms
                1000 * 60 * 60
        );

        String token = shortLivedProvider.createAccessToken("test@example.com", UserType.NORMAL);

        Thread.sleep(10); // token 만료 대기

        // when & then
        assertThatThrownBy(() -> shortLivedProvider.validateToken(token))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("잘못된 서명 토큰은 예외를 던진다.")
    void 잘못된_서명_토큰은_예외를_던진다() {

        String token = tokenProvider.createAccessToken("test@example.com", UserType.ADMIN);

        TokenProvider otherProvider = new TokenProvider(
                "cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdc",
                1000 * 60,
                1000 * 60
        );

        // when & then
        assertThatThrownBy(() -> otherProvider.validateToken(token))
                .isInstanceOf(BusinessException.class);
    }
}