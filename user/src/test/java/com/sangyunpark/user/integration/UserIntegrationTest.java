package com.sangyunpark.user.integration;

import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.entity.UserAddress;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.sangyunpark.user.constant.ResponseMessages.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@SuppressWarnings("NonAsciiCharacters")
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserJpaRepository userRepository;

    @Test
    @DisplayName("정상적인 회원가입 요청이 오면 DB에 유저가 저장된다.")
    void 회원가입_성공() throws Exception {
        // given
        String requestJson = """
            {
              "email": "test@example.com",
              "username": "상윤",
              "password": "password123",
              "phoneNumber": "010-1234-5678",
              "registerType": "EMAIL",
              "userType": "NORMAL",
              "shippingInfo": {
                "receiverName": "홍길동",
                "address": "서울시 강남구",
                "defaultAddress": true
              }
            }
        """;

        // when & then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.message").value(SUCCESS_SIGNUP.message()));

        User user = userRepository.findById(1L).orElseThrow();

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getUsername()).isEqualTo("상윤");
        assertThat(user.getPassword()).isNotEqualTo("password123");
        assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(user.getRegisterType().name()).isEqualTo("EMAIL");
        assertThat(user.getUserType().name()).isEqualTo("NORMAL");
        assertThat(user.getUserStatus().name()).isEqualTo("ACTIVE");

        assertThat(user.getUserAddress()).hasSize(1);
        UserAddress address = user.getUserAddress().get(0);
        assertThat(address.getReceiverName()).isEqualTo("홍길동");
        assertThat(address.getAddress()).isEqualTo("서울시 강남구");
        assertThat(address.getDefaultAddress()).isTrue();

        assertThat(address.getUser()).isEqualTo(user);
    }
}
