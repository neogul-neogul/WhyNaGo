package com.neogul.whynago.common.exception;

public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    // 외부 API·인프라 예외를 도메인 에러코드로 변환할 때 원인을 보존해 로그에서 실제 장애 원인을 추적할 수 있게 한다.
    public BusinessException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }
}