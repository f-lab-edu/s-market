package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class UserServiceTest {

    private UserMapper userMapper;

    private UserJpaRepository repository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        repository = mock(UserJpaRepository.class);
        userService = new UserService(userMapper, repository);
    }

    @Test
    @DisplayName("회원가입 요청 시 User가 저장되고 ID를 반환한다")
    void 회원가입_성공() {
        // given
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("홍길동", "서울시 강남구", true)
        );

        User mappedUser = User.builder()
                .email(dto.email())
                .username(dto.username())
                .password(dto.password())
                .registerType(dto.registerType())
                .phoneNumber(dto.phoneNumber())
                .userType(dto.userType())
                .userStatus(UserStatus.ACTIVE)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email(dto.email())
                .build();

        when(userMapper.toEntity(dto)).thenReturn(mappedUser);
        when(repository.save(mappedUser)).thenReturn(savedUser);

        // when
        Long result = userService.signup(dto);

        // then
        assertThat(result).isEqualTo(1L);
        verify(userMapper).toEntity(dto);
        verify(repository).save(mappedUser);
    }
}