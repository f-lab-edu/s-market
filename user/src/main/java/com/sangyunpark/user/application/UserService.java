package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final UserJpaRepository repository;

    @Transactional
    public Long signup(UserSignupRequestDto userSignupRequestDto) {
        User user = userMapper.toEntity(userSignupRequestDto);
        User savedUser = repository.save(user);

        return savedUser.getId();
    }
}
