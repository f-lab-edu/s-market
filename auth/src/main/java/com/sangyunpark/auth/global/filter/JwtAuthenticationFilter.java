package com.sangyunpark.auth.global.filter;

import com.sangyunpark.auth.jwt.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;

    @SneakyThrows
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String token = resolveToken(request);

        if(isValidToken(token)) {
           setAuthenticationContext(token);
        }

        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String token) {
        return token != null && tokenProvider.validateToken(token);
    }

    private void setAuthenticationContext(String token) {
        final String email = tokenProvider.getEmail(token);
        final String userType = tokenProvider.getUserType(token);

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, userType, List.of(new SimpleGrantedAuthority(userType)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String resolveToken(final HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION_HEADER);
        return header != null ? header.substring(BEARER_PREFIX.length()) : null;
    }
}
