package com.sangyunpark.user.integration;

import com.sangyunpark.user.constant.code.ErrorCode;
import com.sangyunpark.user.domain.entity.User;
import com.sangyunpark.user.domain.entity.UserAddress;
import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.constant.enums.UserType;
import com.sangyunpark.user.exception.BusinessException;
import com.sangyunpark.user.infrastructure.repository.UserJpaRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .andExpect(jsonPath("$.userId").exists());

        User savedUser = userRepository.findUserByEmail("test@example.com")
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
        assertThat(savedUser.getUsername()).isEqualTo("상윤");
        assertThat(savedUser.getPassword()).isNotEqualTo("password123");
        assertThat(savedUser.getPhoneNumber()).isEqualTo("010-1234-5678");
        assertThat(savedUser.getRegisterType().name()).isEqualTo("EMAIL");
        assertThat(savedUser.getUserType().name()).isEqualTo("NORMAL");
        assertThat(savedUser.getUserStatus().name()).isEqualTo("ACTIVE");

        assertThat(savedUser.getUserAddress()).hasSize(1);
        UserAddress address = savedUser.getUserAddress().get(0);
        assertThat(address.getReceiverName()).isEqualTo("홍길동");
        assertThat(address.getAddress()).isEqualTo("서울시 강남구");
        assertThat(address.getDefaultAddress()).isTrue();
        assertThat(address.getUser()).isEqualTo(savedUser);
    }

    @Test
    @DisplayName("중복된 이메일로 회원가입 요청이 오면 400과 예외 메시지를 반환한다.")
    void 회원가입_중복된_이메일_실패() throws Exception {
        // given: 이미 존재하는 유저를 DB에 저장
        User existingUser = User.builder()
                .email("test@example.com")
                .username("기존유저")
                .password("hashedPassword")
                .phoneNumber("010-1234-5678")
                .registerType(RegisterType.EMAIL)
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .build();

        UserAddress address = UserAddress.builder()
                .receiverName("기존수신자")
                .address("서울시 중구")
                .defaultAddress(true)
                .build();

        existingUser.addUserAddress(address);
        userRepository.save(existingUser);

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
                .andExpect(status().is(ErrorCode.USER_DUPLICATE.getStatus().value()));
    }
}
