package com.sangyunpark.user.constant;

public class ValidationMessages {
    public static final String EMAIL_INVALID = "이메일 형식이 올바르지 않습니다.";
    public static final String EMAIL_REQUIRED = "이메일은 필수로 입력해야 합니다.";

    public static final String USERNAME_REQUIRED = "사용자 이름은 필수로 작성해야 합니다.";
    public static final String USERNAME_LENGTH = "이름은 2자 이상 10자 이하로 입력해야 합니다.";

    public static final String PASSWORD_REQUIRED = "비밀번호는 필수로 입력해야 합니다.";
    public static final String PASSWORD_LENGTH = "비밀번호는 8자 이상 20자 이하여야 합니다.";

    public static final String PHONE_REQUIRED = "전화번호는 필수입니다.";
    public static final String PHONE_INVALID = "전화번호 형식이 올바르지 않습니다.";
    public static final String PHONE_REGEX = "^010-?\\d{4}-?\\d{4}$";

    public static final String REGISTER_TYPE_REQUIRED = "가입 유형은 필수입니다.";

    public static final String SHIPPING_INFO_REQUIRED = "배송지 정보는 필수입니다.";

    public static final String RECEIVER_NAME_REQUIRED = "수령인 이름은 필수입니다.";
    public static final String ADDRESS_REQUIRED = "주소 정보는 필수입니다.";

    public static final String USERTYPE_REQUIRED = "회원 유형은 필수입니다.";
}
