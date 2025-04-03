package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.constant.enums.UserType;
import com.sangyunpark.user.exception.UserDuplicateException;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.sangyunpark.user.constant.message.ExceptionMessages.EXCEPTION_USER_DUPLICATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class UserServiceTest {

    private UserMapper userMapper;

    private UserJpaRepository userJpaRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        userJpaRepository = mock(UserJpaRepository.class);
        userService = new UserService(userMapper, userJpaRepository);
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
                new UserAddressRequestDto("박상윤", "서울시 강남구", true)
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
        when(userJpaRepository.save(mappedUser)).thenReturn(savedUser);

        // when
        Long result = userService.signup(dto);

        // then
        assertThat(result).isEqualTo(1L);
        verify(userMapper).toEntity(dto);
        verify(userJpaRepository).save(mappedUser);
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입하면 예외가 발생한다")
    void 중복된_이메일_회원가입_실패() {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "existing@email.com",
                "박상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("박상윤", "서울시 강남구", true)
        );

        User existingUser = User.builder()
                .email(request.email())
                .username(request.username())
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .registerType(request.registerType())
                .userType(request.userType())
                .userStatus(UserStatus.ACTIVE)
                .build();

        given(userJpaRepository.findUserByEmail(request.email()))
                .willReturn(Optional.of(existingUser));

        // when & then
        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(UserDuplicateException.class)
                .hasMessage(EXCEPTION_USER_DUPLICATE.message());
    }
}