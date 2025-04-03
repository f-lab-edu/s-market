package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.exception.UserDuplicateException;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.sangyunpark.user.constant.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserJpaRepository userJpaRepository;

    @Transactional
    public Long signup(UserSignupRequestDto userSignupRequestDto) {
        User user = userMapper.toEntity(userSignupRequestDto);
        userJpaRepository.findUserByEmail(userSignupRequestDto.email())
                .ifPresent(u -> { throw new UserDuplicateException(EXCEPTION_USER_DUPLICATE.message()); });
        User savedUser = userJpaRepository.save(user);

        return savedUser.getId();
    }
}
