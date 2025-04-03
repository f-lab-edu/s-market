package com.sangyunpark.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.domain.dto.response.UserSelectResponseDto;
import com.sangyunpark.user.domain.dto.resquest.UserSelectByEmailRequestDto;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import com.sangyunpark.user.exception.UserNotFoundException;
import com.sangyunpark.user.global.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.sangyunpark.user.constant.ExceptionMessages.EXCEPTION_NOT_FOUND_USER;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

        given(userService.findUserByEmail(any(UserSelectByEmailRequestDto.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/users/email")
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
        UserSelectByEmailRequestDto request = new UserSelectByEmailRequestDto("nonexist@example.com");

        given(userService.findUserByEmail(any(UserSelectByEmailRequestDto.class)))
                .willThrow(new UserNotFoundException(EXCEPTION_NOT_FOUND_USER.message()));

        // when & then
        mockMvc.perform(post("/api/v1/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(content().string(EXCEPTION_NOT_FOUND_USER.message()));
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
                .willThrow(new UserNotFoundException(EXCEPTION_NOT_FOUND_USER.message()));

        // when & then
        mockMvc.perform(get("/api/v1/users/{id}", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string(EXCEPTION_NOT_FOUND_USER.message()));
    }
}