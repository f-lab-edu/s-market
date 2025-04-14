package org.sangyunpark.gateway.filter.matcher;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class UrlMatcherTest {
    @Test
    @DisplayName("URL 경로에 admin이 포함되면 true 반환")
    void isAdminUrl_성공() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/admin/dashboard").build();
        assertThat(UrlMatcher.isAdminUrl(request)).isTrue();
    }

    @Test
    @DisplayName("URL 경로에 admin이 포함되지 않으면 false 반환")
    void isAdminUrl_실패() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/users").build();
        assertThat(UrlMatcher.isAdminUrl(request)).isFalse();
    }

    @Test
    @DisplayName("정확한 로그아웃 경로일 경우 true 반환")
    void isLogoutUrl_성공() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/logout").build();
        assertThat(UrlMatcher.isLogOutUrl(request)).isTrue();
    }

    @Test
    @DisplayName("로그아웃 경로가 아니라면 false 반환")
    void isLogoutUrl_실패() {
        ServerHttpRequest request = MockServerHttpRequest.get("/api/v1/auth/login").build();
        assertThat(UrlMatcher.isLogOutUrl(request)).isFalse();
    }
}