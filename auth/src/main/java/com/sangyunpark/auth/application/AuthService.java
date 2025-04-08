package com.sangyunpark.auth.application;

import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserSelectResponseDto;
import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.domain.vo.Token;
import com.sangyunpark.auth.exception.BusinessException;
import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.LoginResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserClient userClient;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTokenRepository redisTokenRepository;

    public LoginResponseDto login(final LoginRequestDto loginRequestDto) {

        final FeignUserSelectResponseDto user = getFeignUserByEmail(loginRequestDto.email());
        validatePassword(loginRequestDto.password(), user.password());

        final Token token = createToken(user.email(), user.userType());
        saveRefreshToken(user.email(), token.refreshToken());

        return new LoginResponseDto(token);
    }

    private FeignUserSelectResponseDto getFeignUserByEmail(final String email) {
        return userClient.findUserByEmail(email);
    }

    private void validatePassword(final String password, final String encodedPassword) {
        if(!passwordEncoder.matches(password, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private Token createToken(final String email, final UserType userType) {
        final String accessToken = tokenProvider.createAccessToken(email, userType);
        final String refreshToken = tokenProvider.createRefreshToken(email, userType);
        return Token.of(accessToken, refreshToken);
    }

    private void saveRefreshToken(final String email, final String refreshToken) {
        redisTokenRepository.save(email, refreshToken);
    }
}
