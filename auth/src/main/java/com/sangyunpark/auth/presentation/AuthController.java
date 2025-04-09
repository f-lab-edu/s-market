package com.sangyunpark.auth.presentation;

import com.sangyunpark.auth.application.AuthService;
import com.sangyunpark.auth.jwt.UserPrincipal;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String HEADER_REFRESH_TOKEN = "X-Refresh-Token";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/reissue")
    public TokenResponseDto reissue(@RequestHeader(HEADER_REFRESH_TOKEN) final String refreshToken, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return authService.reissue(refreshToken, userPrincipal);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(HEADER_AUTHORIZATION) final String accessToken) {
        authService.logout(accessToken.substring(7));
    }
}
