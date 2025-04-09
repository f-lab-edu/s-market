package com.sangyunpark.auth.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.jwt.TokenValidator;
import com.sangyunpark.auth.jwt.UserPrincipal;
import com.sangyunpark.auth.presentation.dto.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String REISSUE_URL = "/api/v1/auth/issue";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final TokenValidator tokenValidator;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String url = request.getRequestURI();
        if(isLoginOrReissueRequest(url)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = resolveToken(request);

        if(!isValidToken(token)) {
            handleInvalidTokenResponse(response);
            return;
        }

        setAuthenticationContext(token);
        filterChain.doFilter(request, response);
    }

    private boolean isLoginOrReissueRequest(final String url) {
        return url.startsWith(LOGIN_URL) || url.startsWith(REISSUE_URL);
    }

    private boolean isValidToken(final String token) {
        return tokenValidator.validateAccessToken(token);
    }

    private void setAuthenticationContext(final String token) {
        final String email = tokenProvider.getEmail(token);
        final String userType = tokenProvider.getUserType(token);
        final String userStatus = tokenProvider.getUserStatus(token);

        UserPrincipal userPrincipal = new UserPrincipal(email, userType, userStatus);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, List.of(new SimpleGrantedAuthority(userType)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(final HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION_HEADER);
        return header != null ? header.substring(BEARER_PREFIX.length()) : null;
    }

    private void handleInvalidTokenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(ErrorCode.INVALID_TOKEN.getStatus().value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(ErrorCode.INVALID_TOKEN.getCode())));
    }
}
