package com.sangyunpark.user.domain.dto.request;

import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserType;
import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class UserSignupRequestDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    private UserAddressRequestDto validAddress() {
        return new UserAddressRequestDto("홍길동", "서울시 강남구", true);
    }

    @Test
    void 모든값이_정상일때_검증에_성공한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                validAddress()
        );

        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void 이메일이_비어있으면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                validAddress()
        );

        assertViolation(dto, "email");
    }

    @Test
    void 비밀번호가_짧으면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "abcde",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                validAddress()
        );

        assertViolation(dto, "password");
    }

    @Test
    void 전화번호_형식이_잘못되면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010123@@@@45678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                validAddress()
        );

        assertViolation(dto, "phoneNumber");
    }

    @Test
    void 주소가_null이면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                UserType.NORMAL,
                null
        );

        assertViolation(dto, "shippingInfo");
    }

    @Test
    void 회원유형이_null이면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                null,
                validAddress()
        );

        assertViolation(dto, "userType");
    }

    private void assertViolation(UserSignupRequestDto dto, String field) {
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(dto);
        assertThat(violations)
                .hasSize(1)
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)
                );
    }
}