package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.List;

// AI가 만든 서술형 문항 1개의 원본이다. 검증을 통과하기 전이라 아직 문항이 아니다.
// AI 구조화 출력 대상이기도 하므로 필드는 평범한 값 타입만 둔다.
public record GeneratedEssay(
        String title,
        String content,
        String modelAnswer,
        List<String> gradingCriteria,
        Category category,
        Difficulty difficulty,
        List<String> tags
) {
}
