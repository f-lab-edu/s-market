package com.sangyunpark.auth.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.auth.client.dto.response.FeignUserResponseDto;
import com.sangyunpark.auth.constants.code.ErrorCode;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
public class UserClientTest {

    @MockitoBean
    private UserClient userClient; // UserClient를 Mocking

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("호출을 성공해서 회원 정보를 가져옵니다.")
    void 호출_성공_회원_정보_가져오기() {
        // given
        String email = "test@example.com";
        FeignUserResponseDto mockResponse = FeignUserResponseDto.builder()
                .email(email)
                .username("Test User")
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .build();

        given(userClient.findUserByEmail(email)).willReturn(mockResponse);

        // when
        FeignUserResponseDto response = userClient.findUserByEmail(email);

        // then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo(email);
        assertThat(response.username()).isEqualTo("Test User");
        assertThat(response.userType()).isEqualTo(UserType.NORMAL);
        assertThat(response.userStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("FeignClient 요청 실패 시 NoFallbackAvailableException 처리된다.")
    void testFeignClientExceptionHandling() throws Exception {
        // given
        String email = "test@example.com";
        String password = "password123";

        when(userClient.findUserByEmail(email)).thenThrow(new RuntimeException());

        // when & then
        mockMvc.perform(post("/api/v1/auth/login") // 실제 요청 경로
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, password)))
                )
                .andExpect(status().is(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value()))
                .andExpect(jsonPath("$.code").value(ErrorCode.INTERNAL_SERVER_ERROR.getCode()));
    }
}