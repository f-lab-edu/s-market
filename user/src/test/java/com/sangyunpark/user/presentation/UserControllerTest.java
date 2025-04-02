package com.sangyunpark.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.user.application.UserService;
import com.sangyunpark.user.constant.ResponseMessages;
import com.sangyunpark.user.domain.dto.request.UserAddressRequestDto;
import com.sangyunpark.user.domain.dto.request.UserSignupRequestDto;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SuppressWarnings("NonAsciiCharacters")
@WebMvcTest(UserController.class)
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
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("홍길동", "서울시 강남구", true)
        );

        Long fakeUserId = 1L;
        given(userService.signup(Mockito.any())).willReturn(fakeUserId);

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(fakeUserId.intValue())))
                .andExpect(jsonPath("$.message", is(ResponseMessages.SUCCESS_SIGNUP.message())));
    }

    @Test
    @DisplayName("이메일이 비어있으면 400 Bad Request를 반환한다")
    void 이메일이_비어있어_회원가입_실패() throws Exception {
        UserSignupRequestDto request = new UserSignupRequestDto(
                "",  // invalid email
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                new UserAddressRequestDto("홍길동", "서울시 강남구", true)
        );

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}