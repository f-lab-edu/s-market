package com.sangyunpark.product.presentation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class CategoryRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("카테고리 요청 DTO 유효성 검사 성공")
    void 카테고리_요청_DTO_유효성_검사_성공() {
        // given
        CategoryRequestDto dto = new CategoryRequestDto("디지털", 1L);

        // when
        Set<ConstraintViolation<CategoryRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("카테고리 요청 DTO - name이 비어 있으면 실패")
    void 카테고리_요청_DTO_name이_비어있는_경우() {
        // given
        CategoryRequestDto dto = new CategoryRequestDto("  ", 1L); // 공백은 @NotBlank에 걸림

        // when
        Set<ConstraintViolation<CategoryRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }

    @Test
    @DisplayName("카테고리 요청 DTO - name이 null이면 실패")
    void 카테고리_요청_DTO_name이_null인경우() {
        // given
        CategoryRequestDto dto = new CategoryRequestDto(null, 1L);

        // when
        Set<ConstraintViolation<CategoryRequestDto>> violations = validator.validate(dto);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("name");
    }
}