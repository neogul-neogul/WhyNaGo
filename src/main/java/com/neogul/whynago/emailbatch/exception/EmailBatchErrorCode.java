package com.neogul.whynago.emailbatch.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum EmailBatchErrorCode implements ErrorCode {

    EMAIL_BATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "EMAIL_BATCH_NOT_FOUND", "존재하지 않는 발송 이력입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    EmailBatchErrorCode(HttpStatus status, String code, String message) {
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
