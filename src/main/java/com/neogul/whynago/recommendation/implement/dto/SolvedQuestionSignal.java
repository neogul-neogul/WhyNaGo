package com.neogul.whynago.recommendation.implement.dto;

import com.neogul.whynago.mastery.domain.SolvedSignal;

// 객관식·서술형 이력에서 출처를 지운 "문항 1건에 대한 풀이 신호".
public record SolvedQuestionSignal(
        Long questionId,
        SolvedSignal signal
) {
}
