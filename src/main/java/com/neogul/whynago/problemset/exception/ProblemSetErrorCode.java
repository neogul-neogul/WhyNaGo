package com.neogul.whynago.problemset.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ProblemSetErrorCode implements ErrorCode {

    PROBLEM_SET_NOT_FOUND(HttpStatus.NOT_FOUND, "PROBLEM_SET_NOT_FOUND", "문제집을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ProblemSetErrorCode(HttpStatus status, String code, String message) {
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
