package com.sangyunpark.user.integration;

import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.entity.UserAddress;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import com.sangyunpark.user.infrastructure.repository.JpaUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.sangyunpark.user.constant.ExceptionMessages.EXCEPTION_NOT_FOUND_USER;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("NonAsciiCharacters")
class UserQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JpaUserRepository userRepository;

    @Test
    @DisplayName("ID 기반 회원 조회 성공")
    void 회원조회_ID_성공() throws Exception {
        // given
        User user = createAndSaveTestUser("iduser@example.com");

        // when & then
        mockMvc.perform(get("/api/v1/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.email", is("iduser@example.com")))
                .andExpect(jsonPath("$.username", is("상윤")))
                .andExpect(jsonPath("$.userType", is("NORMAL")))
                .andExpect(jsonPath("$.userStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.registerType", is("EMAIL")))
                .andExpect(jsonPath("$.phoneNumber", is("010-1234-5678")));
    }

    @Test
    @DisplayName("ID 기반 회원 조회 실패 - 존재하지 않음")
    void 회원조회_ID_실패() throws Exception {
        mockMvc.perform(get("/api/v1/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(EXCEPTION_NOT_FOUND_USER.message()));
    }

    @Test
    @DisplayName("이메일 기반 회원 조회 성공")
    void 회원조회_이메일_성공() throws Exception {
        // given
        User user = createAndSaveTestUser("emailuser@example.com");

        String requestJson = """
            {
              "email": "emailuser@example.com"
            }
        """;

        // when & then
        mockMvc.perform(post("/api/v1/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(user.getId().intValue())))
                .andExpect(jsonPath("$.email", is("emailuser@example.com")))
                .andExpect(jsonPath("$.username", is("상윤")))
                .andExpect(jsonPath("$.userType", is("NORMAL")))
                .andExpect(jsonPath("$.userStatus", is("ACTIVE")))
                .andExpect(jsonPath("$.registerType", is("EMAIL")))
                .andExpect(jsonPath("$.phoneNumber", is("010-1234-5678")));
    }

    @Test
    @DisplayName("이메일 기반 회원 조회 실패 - 존재하지 않음")
    void 회원조회_이메일_실패() throws Exception {
        String requestJson = """
            {
              "email": "notfound@example.com"
            }
        """;

        mockMvc.perform(post("/api/v1/users/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(content().string(EXCEPTION_NOT_FOUND_USER.message()));
    }

    private User createAndSaveTestUser(String email) {
        User user = User.builder()
                .email(email)
                .username("상윤")
                .password("encodedPassword")
                .phoneNumber("010-1234-5678")
                .registerType(RegisterType.EMAIL)
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .build();

        UserAddress address = UserAddress.builder()
                .receiverName("홍길동")
                .address("서울시 강남구")
                .defaultAddress(true)
                .build();

        user.addUserAddress(address);
        return userRepository.save(user);
    }
}
