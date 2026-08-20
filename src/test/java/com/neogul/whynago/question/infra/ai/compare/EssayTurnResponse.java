package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;

/**
 * 한 턴의 호출 결과. metrics는 토큰 사용량 로그를 못 읽은 경우 null이다.
 */
public record EssayTurnResponse(
        int turn,
        String question,
        String answer,
        boolean followupRequested,
        GradeAndFollowupResult result,
        long elapsedMs,
        AiCallMetrics metrics
) {
}
