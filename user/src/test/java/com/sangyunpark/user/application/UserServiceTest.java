package com.sangyunpark.user.application;

import com.sangyunpark.user.application.mapper.UserMapper;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.resquest.UserSelectByEmailRequestDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import com.sangyunpark.user.exception.UserNotFoundException;
import com.sangyunpark.user.infrastructure.repository.JpaUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.sangyunpark.user.constant.ExceptionMessages.EXCEPTION_NOT_FOUND_USER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserMapper userMapper;
    private JpaUserRepository jpaUserRepository;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = mock(UserMapper.class);
        jpaUserRepository = mock(JpaUserRepository.class);
        userService = new UserService(userMapper, jpaUserRepository);
    }

    @Test
    @DisplayName("ID로 회원 조회 성공")
    void findUserById_성공() {
        // given
        Long id = 1L;
        User user = createUser();
        UserSelectResponseDto dto = createResponseDto(user);

        given(jpaUserRepository.findById(id)).willReturn(Optional.of(user));
        given(userMapper.toUserSelectResponseDto(user)).willReturn(dto);

        // when
        UserSelectResponseDto result = userService.findUserById(id);

        // then
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("ID로 존재하지 않는 회원 조회 시 예외 발생")
    void findUserById_실패() {
        Long id = 999L;

        given(jpaUserRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(id))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(EXCEPTION_NOT_FOUND_USER.message());
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void findUserByEmail_성공() {
        // given
        User user = createUser();
        UserSelectByEmailRequestDto dto = new UserSelectByEmailRequestDto("test@example.com");
        UserSelectResponseDto expectedDto = createResponseDto(user);

        given(jpaUserRepository.findUserByEmail(dto.email())).willReturn(Optional.of(user));
        given(userMapper.toUserSelectResponseDto(user)).willReturn(expectedDto);

        // when
        UserSelectResponseDto result = userService.findUserByEmail(dto);

        // then
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(jpaUserRepository).findUserByEmail(dto.email());
    }

    @Test
    @DisplayName("이메일로 존재하지 않는 회원 조회 시 예외 발생")
    void findUserByEmail_실패() {
        UserSelectByEmailRequestDto dto = new UserSelectByEmailRequestDto("notfound@example.com");
        given(jpaUserRepository.findUserByEmail(dto.email())).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail(dto))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage(EXCEPTION_NOT_FOUND_USER.message());
    }

    private User createUser() {
        return User.builder()
                .id(1L)
                .email("test@example.com")
                .username("상윤")
                .password("hashed-password")
                .phoneNumber("010-1234-5678")
                .registerType(RegisterType.EMAIL)
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    private UserSelectResponseDto createResponseDto(User user) {
        return UserSelectResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .userType(user.getUserType())
                .userStatus(user.getUserStatus())
                .registerType(user.getRegisterType())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }
}