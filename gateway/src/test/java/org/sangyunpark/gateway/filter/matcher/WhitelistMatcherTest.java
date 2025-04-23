package org.sangyunpark.gateway.filter.matcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class WhitelistMatcherTest {

    private final WhitelistMatcher whitelistMatcher;

    WhitelistMatcherTest() {
        whitelistMatcher = new WhitelistMatcher();
    }

    @Test
    @DisplayName("POST /api/v1/users 는 화이트리스트에 포함됨")
    void 화이트리스트_포함_유저생성() {
        ServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/users")
                .build();

        assertThat(whitelistMatcher.isWhitelisted(request)).isTrue();
    }

    @Test
    @DisplayName("POST /api/v1/auth/login 은 화이트리스트에 포함됨")
    void 화이트리스트_포함_로그인() {
        ServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/auth/login")
                .build();

        assertThat(whitelistMatcher.isWhitelisted(request)).isTrue();
    }

    @Test
    @DisplayName("GET 요청은 화이트리스트에 포함되지 않음")
    void 화이트리스트_미포함_메서드불일치() {
        ServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users")
                .build();

        assertThat(whitelistMatcher.isWhitelisted(request)).isFalse();
    }

    @Test
    @DisplayName("정의되지 않은 경로는 화이트리스트에 포함되지 않음")
    void 화이트리스트_미포함_경로불일치() {
        ServerHttpRequest request = MockServerHttpRequest
                .post("/api/v1/products")
                .build();

        assertThat(whitelistMatcher.isWhitelisted(request)).isFalse();
    }
}