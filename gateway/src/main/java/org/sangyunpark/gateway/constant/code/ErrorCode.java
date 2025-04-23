package org.sangyunpark.gateway.constant.code;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_TOKEN("INVALID_TOKEN", HttpStatus.UNAUTHORIZED);

    private final String code;
    private final HttpStatus status;

    ErrorCode(String errorCode, HttpStatus httpStatus) {
        this.code = errorCode;
        this.status = httpStatus;
    }
}
