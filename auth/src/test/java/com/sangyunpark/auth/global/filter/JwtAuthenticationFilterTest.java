package com.sangyunpark.auth.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.jwt.TokenValidator;
import com.sangyunpark.auth.jwt.UserPrincipal;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        tokenProvider = mock(TokenProvider.class);
        tokenValidator = mock(TokenValidator.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        filter = new JwtAuthenticationFilter(tokenProvider, tokenValidator, objectMapper);
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
        String status = "ACTIVE";

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);

        when(tokenValidator.validateAccessToken(anyString())).thenReturn(true);
        when(tokenProvider.getEmail(token)).thenReturn(email);
        when(tokenProvider.getUserType(token)).thenReturn(userType);
        when(tokenProvider.getUserStatus(token)).thenReturn(status);

        // when
        filter.doFilterInternal(request, response, filterChain);

        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // then
        assertThat(principal).isNotNull();
        assertThat(principal).isInstanceOf(UserPrincipal.class);
        assertThat(principal.getEmail()).isEqualTo(email);
        assertThat(principal.getUserType().name()).isEqualTo(userType);
        assertThat(principal.getUserStatus().name()).isEqualTo(status);

        verify(filterChain).doFilter(request, response);
    }
}