package com.neogul.whynago.question.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum QuestionErrorCode implements ErrorCode {

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_NOT_FOUND", "문제를 찾을 수 없습니다."),
    CHOICE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHOICE_NOT_FOUND", "선택지를 찾을 수 없습니다."),
    CHOICE_NOT_IN_QUESTION(HttpStatus.BAD_REQUEST, "CHOICE_NOT_IN_QUESTION", "선택지가 해당 문제에 속하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    QuestionErrorCode(HttpStatus status, String code, String message) {
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
