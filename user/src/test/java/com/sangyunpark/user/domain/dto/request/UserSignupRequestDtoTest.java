package com.sangyunpark.user.domain.dto.request;

import com.sangyunpark.user.domain.vo.RegisterType;
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

    private AddressRequestDto validAddress() {
        return new AddressRequestDto("홍길동", "서울시 강남구", true);
    }

    @Test
    void 모든값이_정상일때_검증에_성공한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
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
                validAddress()
        );

        assertViolation(dto, "email", "이메일은 필수로 입력해야 합니다.");
    }

    @Test
    void 비밀번호가_짧으면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "abcde",
                "010-1234-5678",
                RegisterType.EMAIL,
                validAddress()
        );

        assertViolation(dto, "password", "비밀번호는 8자 이상 20자 이하여야 합니다.");
    }

    @Test
    void 전화번호_형식이_잘못되면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "01012345678",
                RegisterType.EMAIL,
                validAddress()
        );

        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void 주소가_null이면_검증에_실패한다() {
        UserSignupRequestDto dto = new UserSignupRequestDto(
                "test@example.com",
                "상윤",
                "password123",
                "010-1234-5678",
                RegisterType.EMAIL,
                null
        );

        assertViolation(dto, "address", "주소 정보는 필수입니다.");
    }

    private void assertViolation(UserSignupRequestDto dto, String field, String expectedMessage) {
        Set<ConstraintViolation<UserSignupRequestDto>> violations = validator.validate(dto);
        assertThat(violations)
                .hasSize(1)
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals(field)
                                && v.getMessage().equals(expectedMessage)
                );
    }
}