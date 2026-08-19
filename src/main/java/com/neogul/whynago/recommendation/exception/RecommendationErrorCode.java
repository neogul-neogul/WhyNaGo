package com.neogul.whynago.recommendation.exception;

import com.neogul.whynago.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

// 서술형 채점 쿼터(ESSAY_AI_*)와 버킷을 분리한다. 추천 문제 생성이 채점 쿼터를 잠식하면
// 사용자가 지금 풀고 있는 문제의 채점이 멈춘다.
public enum RecommendationErrorCode implements ErrorCode {

    RECOMMENDATION_AI_UNAVAILABLE(
            HttpStatus.SERVICE_UNAVAILABLE,
            "RECOMMENDATION_AI_UNAVAILABLE",
            "맞춤 문제 생성 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    RECOMMENDATION_AI_QUOTA_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "RECOMMENDATION_AI_QUOTA_EXCEEDED",
            "맞춤 문제 생성 요청이 몰렸습니다. 잠시 후 다시 시도해 주세요."),
    RECOMMENDATION_AI_DAILY_QUOTA_EXCEEDED(
            HttpStatus.TOO_MANY_REQUESTS,
            "RECOMMENDATION_AI_DAILY_QUOTA_EXCEEDED",
            "오늘의 맞춤 문제 생성 한도를 모두 사용했습니다. 내일 다시 이용해 주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

    RecommendationErrorCode(HttpStatus status, String code, String message) {
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
