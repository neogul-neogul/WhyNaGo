package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.question.service.dto.SolvingTimeResult;

// 점수에 시간이 얼마나 반영됐는지를 클라이언트가 그대로 설명할 수 있게 내보낸다.
// 이게 없으면 루브릭 항목 배점 합과 score가 어긋나 보여 버그처럼 읽힌다.
public record SolvingTimeResponse(int elapsedSeconds, int averageSeconds, ElapsedPace pace, int scoreAdjustment) {

    static SolvingTimeResponse from(SolvingTimeResult result) {
        return new SolvingTimeResponse(
                result.elapsedSeconds(), result.averageSeconds(), result.pace(), result.scoreAdjustment());
    }
}
