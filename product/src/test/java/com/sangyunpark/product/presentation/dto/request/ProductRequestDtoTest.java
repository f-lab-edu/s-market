package com.sangyunpark.product.presentation.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("NonAsciiCharacters")
class ProductRequestDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("상품 요청 DTO 유효성 검사 성공")
    void 유효한_요청일_경우_검증성공() {
        ProductRequestDto dto = new ProductRequestDto(
                1L,
                "상품명",
                "설명입니다",
                10000L,
                true,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(1)
        );

        Set<ConstraintViolation<ProductRequestDto>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("필수값이 누락되면 검증 실패")
    void 필수값_누락시_검증실패() {
        ProductRequestDto dto = new ProductRequestDto(
                null,
                "",
                null,
                0L,
                null,
                null,
                null
        );

        Set<ConstraintViolation<ProductRequestDto>> violations = validator.validate(dto);
        assertThat(violations).hasSizeGreaterThan(0);
    }

    @Test
    @DisplayName("가격이 허용 범위를 벗어나면 실패")
    void 가격이_1미만이면_실패() {
        ProductRequestDto dto1 = new ProductRequestDto(
                1L, "상품", "설명", 0L, true,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1)
        );

        assertThat(validator.validate(dto1)).isNotEmpty();
    }
}