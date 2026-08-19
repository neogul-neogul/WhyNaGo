package com.neogul.whynago.common.ai;

// 외부 AI 호출 실패의 성격이다. 도메인별로 에러코드가 달라도 분류 기준은 하나로 둔다.
public enum AiFailureKind {
    UNAVAILABLE,
    QUOTA_EXCEEDED,
    DAILY_QUOTA_EXCEEDED,
}
