package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.constant.code.ErrorCode;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.exception.BusinessException;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sangyunpark.user.application.mapper.UserMapper.toEntity;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserJpaRepository userJpaRepository;

    @Transactional
    public Long signup(final UserSignupRequestDto userSignupRequestDto) {

        return (Long) userJpaRepository.findUserByEmail(userSignupRequestDto.email())
                .map(user -> {
                    throw new BusinessException(ErrorCode.USER_DUPLICATE);
                })
                .orElseGet(() -> {
                    User savedUser = userJpaRepository.save(toEntity(userSignupRequestDto));
                    return savedUser.getId();
                });
    }

    public UserSelectResponseDto findUserById(final Long userId) {
        return userJpaRepository.findById(userId).map(UserMapper::toUserSelectResponseDto).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public UserSelectResponseDto findUserByEmail(final String email) {
        return userJpaRepository.findUserByEmail(email).map(UserMapper::toUserSelectResponseDto).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
