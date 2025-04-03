package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.resquest.UserSelectByEmailRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.exception.UserNotFoundException;
import com.sangyunpark.user.infrastructure.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sangyunpark.user.constant.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JpaUserRepository jpaUserRepository;

    @Transactional(readOnly = true)
    public UserSelectResponseDto findUserById(final Long userId) {
        User user = jpaUserRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(EXCEPTION_NOT_FOUND_USER.message()));
        return userMapper.toUserSelectResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserSelectResponseDto findUserByEmail(final UserSelectByEmailRequestDto dto) {
        User user = jpaUserRepository.findUserByEmail(dto.email()).orElseThrow(() -> new UserNotFoundException(EXCEPTION_NOT_FOUND_USER.message()));
        return userMapper.toUserSelectResponseDto(user);
    }
}
