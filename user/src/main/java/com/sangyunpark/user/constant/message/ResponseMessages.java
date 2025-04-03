package com.sangyunpark.user.constant.message;

public enum ResponseMessages {

    SUCCESS_SIGNUP("회원가입이 완료되었습니다.");

    private final String message;

    ResponseMessages(String message) {
        this.message = message;
    }

    public String message() {
        return message;
    }
}
