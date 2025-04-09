package com.sangyunpark.auth.global.filter;

import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.jwt.TokenValidator;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class JwtAuthenticationFilterTest {

    private TokenProvider tokenProvider;
    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;
    private TokenValidator tokenValidator;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        tokenValidator = mock(TokenValidator.class);
        filter = new JwtAuthenticationFilter(tokenProvider, tokenValidator);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 토큰이면 SecurityContext에 인증 정보가 저장된다.")
    void 유효한_토큰_인증_정보_저장_성공() throws Exception {
        // given
        String email = "test@example.com";
        String userType = "NORMAL";
        String token = "valid-token";

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);

        when(tokenValidator.validateAccessToken(anyString())).thenReturn(true);
        when(tokenProvider.getEmail(token)).thenReturn(email);
        when(tokenProvider.getUserType(token)).thenReturn(userType);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(email);
        assertThat(authentication.getCredentials()).isEqualTo(userType);
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly(userType);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 인증 정보 없이 다음 필터로 넘어간다.")
    void 유효하지_않은_토큰_인증_정보_미설정() throws Exception {
        // given
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(tokenProvider.validateToken(token)).thenReturn(false);

        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 인증 정보 없이 다음 필터로 넘어간다.")
    void 헤더_없는_경우_인증_정보_미설정() throws Exception {
        // given
        // when
        filter.doFilterInternal(request, response, filterChain);

        // then
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }


}