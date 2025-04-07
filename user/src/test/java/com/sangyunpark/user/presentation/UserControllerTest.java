package com.sangyunpark.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserType;
import com.sangyunpark.user.exception.BusinessException;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.request.UserSelectByEmailRequestDto;
import com.sangyunpark.user.global.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.sangyunpark.user.constant.code.ErrorCode.*;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("NonAsciiCharacters")
@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("회원가입 요청이 유효하면 200 OK와 userId, 메시지를 반환한다")
    void 회원가입_성공() throws Exception {
        // given
        UserSignupRequestDto request = new UserSignupRequestDto(
                "test@example.com",
                "박상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("박상윤", "서울시 강남구", true)
        );

        Long fakeUserId = 1L;
        given(userService.signup(any())).willReturn(fakeUserId);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(fakeUserId.intValue())));
    }

    @Test
    @DisplayName("중복된_이메일이면_회원가입_실패")
    void 중복된_이메일이면_회원가입_실패() throws Exception {
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

        given(userService.signup(any(UserSignupRequestDto.class)))
                .willThrow(new BusinessException(USER_DUPLICATE));

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(USER_DUPLICATE.getStatus().value()));
    }

    @Test
    @DisplayName("이메일이 비어있으면 400 Bad Request를 반환한다")
    void 이메일이_비어있어_회원가입_실패() throws Exception {
        UserSignupRequestDto request = new UserSignupRequestDto(
                "",  // invalid email
                "박상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("박상윤", "서울시 강남구", true)
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(INVALID_REQUEST.getStatus().value()));
    }

    @Test
    @DisplayName("이메일로 유저 조회 성공")
    void 이메일로_유저_조회_성공() throws Exception {
        // given
        UserSelectByEmailRequestDto request = new UserSelectByEmailRequestDto("test@example.com");
        UserSelectResponseDto response = UserSelectResponseDto.builder()
                .id(1L)
                .email("test@example.com")
                .username("상윤")
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .registerType(RegisterType.EMAIL)
                .phoneNumber("010-1234-5678")
                .build();

        given(userService.findUserByEmail(any(String.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users?email=test@example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.username", is("상윤")))
                .andExpect(jsonPath("$.userType", is("NORMAL")))
                .andExpect(jsonPath("$.userStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.registerType", is("EMAIL")))
                .andExpect(jsonPath("$.phoneNumber", is("010-1234-5678")));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 유저 조회 시 예외 발생")
    void 이메일로_유저_조회_실패() throws Exception {
        // given
        given(userService.findUserByEmail(any(String.class)))
                .willThrow(new BusinessException(USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/users?email=nonexist@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("ID로 유저 조회 성공")
    void 유저_ID_조회_성공() throws Exception {
        // given
        Long userId = 1L;
        UserSelectResponseDto response = UserSelectResponseDto.builder()
                .id(userId)
                .email("test@example.com")
                .username("상윤")
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .registerType(RegisterType.EMAIL)
                .phoneNumber("010-1234-5678")
                .build();

        given(userService.findUserById(userId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("상윤"));
    }

    @Test
    @DisplayName("존재하지 않는 ID로 유저 조회 시 404 반환")
    void 유저_ID_조회_실패() throws Exception {
        // given
        Long userId = 999L;
        given(userService.findUserById(userId))
                .willThrow(new BusinessException(USER_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound());
    }
}