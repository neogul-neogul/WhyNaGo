package com.neogul.whynago.interview.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum InterviewErrorCode implements ErrorCode {

    INTERVIEW_ALREADY_STARTED_TODAY(HttpStatus.CONFLICT, "INTERVIEW_ALREADY_STARTED_TODAY", "오늘은 이미 면접을 시작했습니다."),
    INTERVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "INTERVIEW_NOT_FOUND", "면접을 찾을 수 없습니다."),
    INTERVIEW_NOT_IN_PROGRESS(HttpStatus.BAD_REQUEST, "INTERVIEW_NOT_IN_PROGRESS", "진행 중인 면접이 아닙니다."),
    INTERVIEW_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "INTERVIEW_NOT_COMPLETED", "아직 완료되지 않은 면접입니다."),
    INTERVIEW_NOT_CANCELABLE(HttpStatus.BAD_REQUEST, "INTERVIEW_NOT_CANCELABLE", "이미 진행된 면접은 취소할 수 없습니다."),
    INTERVIEW_QUESTION_NOT_AVAILABLE(HttpStatus.NOT_FOUND, "INTERVIEW_QUESTION_NOT_AVAILABLE", "출제할 수 있는 서술형 문제가 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    InterviewErrorCode(HttpStatus status, String code, String message) {
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
