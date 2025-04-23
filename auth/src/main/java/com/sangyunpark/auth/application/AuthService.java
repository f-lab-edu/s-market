package com.sangyunpark.auth.application;

import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserResponseDto;
import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.domain.vo.Token;
import com.sangyunpark.auth.exception.BusinessException;
import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.TokenResponseDto;
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

    public TokenResponseDto login(final LoginRequestDto loginRequestDto) {

        final FeignUserResponseDto user = userClient.findUserByEmail(loginRequestDto.email());
        validatePassword(loginRequestDto.password(), user.password());
        Token token = generateAndStoreToken(user.email(), user.userType(), user.userStatus());
        return new TokenResponseDto(token);
    }

    public TokenResponseDto reissue(final String refreshToken, final String email, final String userType, final String userStatus) {
        tokenProvider.validateToken(refreshToken);
        validateStoredRefreshToken(email, refreshToken);
        Token newToken = generateAndStoreToken(email, UserType.valueOf(userType), UserStatus.valueOf(userStatus));
        return new TokenResponseDto(newToken);
    }

    private void validatePassword(final String password, final String encodedPassword) {
        if(!passwordEncoder.matches(password, encodedPassword)) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
    }

    private Token generateAndStoreToken(final String email, final UserType userType, final UserStatus userStatus) {
        final String accessToken = tokenProvider.createAccessToken(email, userType, userStatus);
        final String refreshToken = tokenProvider.createRefreshToken(email, userType, userStatus);

        redisTokenRepository.save(email, refreshToken);
        return new Token(accessToken, refreshToken);
    }

    private void validateStoredRefreshToken(final String email, final String requestToken) {
        redisTokenRepository.findByEmail(email)
                .filter(storedToken -> storedToken.equals(requestToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
    }

    public void logout(final String accessToken)  {
        long remainingTime = tokenProvider.getRemainingExpiration(accessToken);
        redisTokenRepository.saveLogOutToken(accessToken, remainingTime);
    }
}