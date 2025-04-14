package com.sangyunpark.auth.presentation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestDtoTest {

    private Validator validator;

    private static final String VALID_EMAIL = "test@test.com";
    private static final String INVALID_EMAIL = "invalid-test.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";

    @BeforeEach
    void setUp() {
        try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    @DisplayName("유효한 이메일과 비밀번호가 입력되면 검증에 통과한다")
    void validLoginRequest() {
        // given
        LoginRequestDto request = new LoginRequestDto(VALID_EMAIL, VALID_PASSWORD);

        // when
        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(request);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("이메일이 비어 있으면 검증에 실패한다")
    void blankEmailFailsValidation() {
        LoginRequestDto request = new LoginRequestDto("", VALID_PASSWORD);

        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(EMAIL));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 검증에 실패한다")
    void invalidEmailFormatFailsValidation() {
        LoginRequestDto request = new LoginRequestDto(INVALID_EMAIL, VALID_PASSWORD);

        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(EMAIL));
    }

    @Test
    @DisplayName("비밀번호가 비어 있으면 검증에 실패한다")
    void blankPasswordFailsValidation() {
        LoginRequestDto request = new LoginRequestDto(VALID_EMAIL, "");

        Set<ConstraintViolation<LoginRequestDto>> violations = validator.validate(request);

        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals(PASSWORD));
    }
}