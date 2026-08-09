package com.neogul.whynago.user.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_INVALID_EMAIL(HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    USER_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "닉네임 형식이 올바르지 않습니다."),
    USER_DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    USER_DUPLICATE_EMAIL_SOCIAL(HttpStatus.CONFLICT, "구글 계정으로 가입된 이메일입니다. 구글 계정으로 로그인해주세요."),
    USER_DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return name();
    }

    @Override
    public String message() {
        return message;
    }
}
