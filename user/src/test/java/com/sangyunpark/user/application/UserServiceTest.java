package com.sangyunpark.user.application;

import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.constant.enums.UserType;
import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.exception.BusinessException;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SuppressWarnings("NonAsciiCharacters")
class UserServiceTest {

    private UserJpaRepository userJpaRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userJpaRepository = mock(UserJpaRepository.class);
        userService = new UserService(userJpaRepository);
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

        User savedUser = User.builder()
                .id(1L)
                .email(dto.email())
                .build();

        when(userJpaRepository.findUserByEmail(dto.email())).thenReturn(Optional.empty());
        when(userJpaRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        Long result = userService.signup(dto);

        // then
        assertThat(result).isEqualTo(1L);
        verify(userJpaRepository).findUserByEmail(any(String.class));
        verify(userJpaRepository).save(any(User.class));
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
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("ID로 회원 조회 성공")
    void findUserById_성공() {
        // given
        Long id = 1L;
        User user = createUser();

        given(userJpaRepository.findById(id)).willReturn(Optional.of(user));

        // when
        UserSelectResponseDto result = userService.findUserById(id);

        // then
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("ID로 존재하지 않는 회원 조회 시 예외 발생")
    void findUserById_실패() {
        Long id = 999L;

        given(userJpaRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(id))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("이메일로 회원 조회 성공")
    void findUserByEmail_성공() {
        // given
        final User user = createUser();
        String email = "test@example.com";

        given(userJpaRepository.findUserByEmail(email)).willReturn(Optional.of(user));

        // when
        UserSelectResponseDto result = userService.findUserByEmail(email);

        // then
        assertThat(result.email()).isEqualTo("test@example.com");
        verify(userJpaRepository).findUserByEmail(email);
    }

    @Test
    @DisplayName("이메일로 존재하지 않는 회원 조회 시 예외 발생")
    void findUserByEmail_실패() {
        String email = "notfound@example.com";
        given(userJpaRepository.findUserByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByEmail(email))
                .isInstanceOf(BusinessException.class);
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
}