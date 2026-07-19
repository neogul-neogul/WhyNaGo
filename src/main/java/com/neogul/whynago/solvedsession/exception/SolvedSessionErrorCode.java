package com.neogul.whynago.solvedsession.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum SolvedSessionErrorCode implements ErrorCode {

    SOLVED_SESSION_BROKEN_CHAIN(HttpStatus.BAD_REQUEST, "SOLVED_SESSION_BROKEN_CHAIN", "꼬리질문 연결이 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    SolvedSessionErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatus status() {
        return status;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
