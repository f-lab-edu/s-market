package com.sangyunpark.auth.presentation;

import com.sangyunpark.auth.application.AuthService;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.TokenResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String HEADER_REFRESH_TOKEN = "X-Refresh-Token";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_TYPE = "X-User-Type";
    private static final String HEADER_USER_STATUS = "X-User-Status";
    private static final String HEADER_AUTHORIZATION = "Authorization";

    private final AuthService authService;

    @PostMapping("/login")
    public TokenResponseDto login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        return authService.login(loginRequestDto);
    }

    @PostMapping("/reissue")
    public TokenResponseDto reissue(
            @RequestHeader(HEADER_REFRESH_TOKEN) final String refreshToken,
            @RequestHeader(HEADER_USER_EMAIL) final String email,
            @RequestHeader(HEADER_USER_TYPE) final String userType,
            @RequestHeader(HEADER_USER_STATUS) final String userStatus
    ) {
        return authService.reissue(refreshToken, email, userType, userStatus);
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader(HEADER_AUTHORIZATION) final String accessToken) {
        authService.logout(accessToken.substring(7));
    }
}
