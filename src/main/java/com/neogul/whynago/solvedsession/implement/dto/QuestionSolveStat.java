package com.neogul.whynago.solvedsession.implement.dto;

// 객관식·서술형 어느 쪽 이력에서 나왔는지를 지운 문항별 집계 한 건.
public record QuestionSolveStat(
        Long questionId,
        long solvedCount,
        long correctCount,
        Double avgElapsedSeconds
) {
}
