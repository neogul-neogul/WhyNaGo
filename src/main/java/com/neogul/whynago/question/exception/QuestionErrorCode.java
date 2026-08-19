package com.neogul.whynago.question.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum QuestionErrorCode implements ErrorCode {

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUESTION_NOT_FOUND", "문제를 찾을 수 없습니다."),
    QUESTION_NOT_ESSAY(HttpStatus.BAD_REQUEST, "QUESTION_NOT_ESSAY", "서술형 문제가 아닙니다."),
    QUESTION_NOT_MULTIPLE_CHOICE(HttpStatus.BAD_REQUEST, "QUESTION_NOT_MULTIPLE_CHOICE", "객관식 문제가 아닙니다."),
    ESSAY_AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "ESSAY_AI_UNAVAILABLE", "AI 채점·꼬리질문 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    ESSAY_AI_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "ESSAY_AI_QUOTA_EXCEEDED", "AI 채점 요청이 몰렸습니다. 잠시 후 다시 시도해 주세요."),
    ESSAY_AI_DAILY_QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "ESSAY_AI_DAILY_QUOTA_EXCEEDED", "오늘의 AI 채점 한도를 모두 사용했습니다. 내일 다시 이용해 주세요."),
    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "TAG_NOT_FOUND", "태그 사전에 없는 태그입니다."),
    QUESTION_REVIEW_ALREADY_DECIDED(HttpStatus.CONFLICT, "QUESTION_REVIEW_ALREADY_DECIDED", "이미 검수가 완료된 문제입니다."),
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
