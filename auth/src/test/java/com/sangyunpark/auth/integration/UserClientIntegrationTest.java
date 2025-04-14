package com.sangyunpark.auth.integration;

import com.sangyunpark.auth.client.UserClient;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserClientIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserClient userClient; // FeignClient Mocking

    @Test
    @DisplayName("존재하지 않는 유저 요청시 user_not_found 오류코드가 내려온다")
    void shouldReturnUserNotFoundWhenUserDoesNotExist() throws Exception {
        // given
        String email = "notfound@example.com";

        // FeignClient가 404 예외를 던지도록 설정
        Mockito.when(userClient.findUserByEmail(email))
                .thenThrow(FeignException.NotFound.class);

        // when
        mockMvc.perform(get("/api/v1/auth/login") // 실제 사용하는 엔드포인트
                        .param("email", email))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_SERVER_ERROR")); // 비즈니스 코드 확인
    }
}
