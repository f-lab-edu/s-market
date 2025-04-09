package com.sangyunpark.auth.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sangyunpark.auth.client.UserClient;
import com.sangyunpark.auth.client.dto.response.FeignUserSelectResponseDto;
import com.sangyunpark.auth.constants.enums.RegisterType;
import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import com.sangyunpark.auth.infrastructure.repository.RedisTokenRepository;
import com.sangyunpark.auth.jwt.TokenProvider;
import com.sangyunpark.auth.presentation.dto.request.LoginRequestDto;
import com.sangyunpark.auth.presentation.dto.response.TokenResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("NonAsciiCharacters")
public class AuthIntegrationTest {

    private static final String EMAIL = "test@example.com";
    private static final String RAW_PASSWORD = "password123";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    TokenProvider tokenProvider;

    @MockitoBean
    private UserClient userClient;

    @Autowired
    private RedisTokenRepository redisTokenRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    @DisplayName("정상적인 로그인 요청이 오면 토큰 정보를 반환하고, 리프레시 토큰이 Redis에 저장된다.")
    void 로그인_성공_및_리프레시_토큰_저장_검증() throws Exception {
        // given
        String email = EMAIL;
        String rawPassword = RAW_PASSWORD;

        LoginRequestDto request = new LoginRequestDto(email, rawPassword);

        FeignUserSelectResponseDto userResponse = FeignUserSelectResponseDto.builder()
                .id(1L)
                .email(email)
                .password(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(rawPassword))
                .username("상윤")
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .registerType(RegisterType.EMAIL)
                .phoneNumber("010-1234-5678")
                .build();

        when(userClient.findUserByEmail(email)).thenReturn(userResponse);

        // when
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.token.refreshToken").isNotEmpty());

        // then
        assertThat(redisTokenRepository.exists(email)).isTrue();
        assertThat(redisTokenRepository.findByEmail(email)).isPresent();
    }

    @Test
    @DisplayName("동일 유저가 다시 로그인하면 refreshToken이 갱신된다.")
    void 로그인_재시도_refreshToken_갱신() throws Exception {
        String email = "test@example.com";
        LoginRequestDto request = new LoginRequestDto(email, "password123");

        String newEncodedPassword = new BCryptPasswordEncoder().encode("password123");

        FeignUserSelectResponseDto userResponse = FeignUserSelectResponseDto.builder()
                .email(email)
                .password(newEncodedPassword)
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .registerType(RegisterType.EMAIL)
                .username("상윤")
                .phoneNumber("010-1234-5678")
                .build();

        when(userClient.findUserByEmail(email)).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        final String firstRefreshToken = redisTokenRepository.findByEmail(email).orElseThrow();

        Thread.sleep(1000);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        final String secondRefreshToken = redisTokenRepository.findByEmail(email).orElseThrow();

        assertThat(firstRefreshToken).isNotEqualTo(secondRefreshToken);
    }

    @Test
    @DisplayName("로그아웃 요청 시, 해당 accessToken이 블랙리스트에 추가된다.")
    void 로그아웃_요청() throws Exception {
        // given
        String email = "test@example.com";
        LoginRequestDto request = new LoginRequestDto(email, "password123");

        String newEncodedPassword = new BCryptPasswordEncoder().encode("password123");

        FeignUserSelectResponseDto userResponse = FeignUserSelectResponseDto.builder()
                .email(email)
                .password(newEncodedPassword)
                .userType(UserType.NORMAL)
                .userStatus(UserStatus.ACTIVE)
                .registerType(RegisterType.EMAIL)
                .username("상윤")
                .phoneNumber("010-1234-5678")
                .build();

        when(userClient.findUserByEmail(email)).thenReturn(userResponse);

        TokenResponseDto response = objectMapper.readValue(
                mockMvc.perform(post(LOGIN_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.token.accessToken").exists()) // 응답에 accessToken이 포함되었는지 확인
                        .andReturn().getResponse().getContentAsString(), TokenResponseDto.class
        );

        final String accessToken = response.token().accessToken();

        mockMvc.perform(post(LOGOUT_URL)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        assertThat(redisTokenRepository.isLogOutToken(accessToken)).isTrue();
    }

    @AfterEach
    void tearDown() {
        Set<String> keys = stringRedisTemplate.keys("*");
        stringRedisTemplate.delete(keys);
    }
}
