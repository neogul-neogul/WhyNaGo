package com.neogul.whynago.wrongnote.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WrongNoteErrorCode implements ErrorCode {

    WRONG_NOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "WRONG_NOTE_NOT_FOUND", "오답노트를 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    WrongNoteErrorCode(HttpStatus status, String code, String message) {
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