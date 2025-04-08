package com.sangyunpark.auth.integration;

import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.jwt.TokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("NonAsciiCharacters")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    private String createAccessToken(String email, String userType) {
        return tokenProvider.createAccessToken(email, UserType.valueOf(userType), UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("ADMIN 권한이 있는 경우 관리자 API 접근 가능")
    void admin_권한_접근_허용() throws Exception {
        String token = createAccessToken("admin@example.com", "ADMIN");

        mockMvc.perform(get("/api/v1/admin/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN 권한이 없는 경우 관리자 API 접근 거부")
    void admin_권한_없는_경우_접근_거부() throws Exception {
        String token = createAccessToken("user@example.com", "NORMAL");

        mockMvc.perform(get("/api/v1/admin/test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("토큰 없이 요청하면 401 Unauthorized")
    void 토큰_없는_경우_401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/test"))
                .andExpect(status().isUnauthorized());
    }
}
