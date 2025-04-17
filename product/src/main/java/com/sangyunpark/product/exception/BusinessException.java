package com.sangyunpark.product.exception;

import com.sangyunpark.product.constant.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(final ErrorCode errorCode) {
        this.errorCode = errorCode;
    }
}
