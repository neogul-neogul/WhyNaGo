package com.neogul.whynago.question.implement.dto;

// 배치가 계산한 문항별 통계 한 건. avgElapsedSeconds가 null이면 소요 시간 표본이 없다는 뜻이다.
public record QuestionStatSnapshot(
        Long questionId,
        Integer avgElapsedSeconds,
        double correctRate,
        int sampleCount
) {
}
