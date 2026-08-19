package com.neogul.whynago.recommendation.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

public record RecommendedQuestionResult(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        List<String> tags,
        // 이 문항이 이번 추천을 위해 AI가 만든 것인지 여부. 검수 전 문항임을 클라이언트가 표시할 수 있게 내린다.
        boolean generated
) {

    public static RecommendedQuestionResult of(Question question, List<String> tags) {
        return new RecommendedQuestionResult(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getType(),
                question.getDifficulty(),
                question.getCategory(),
                tags,
                question.isGenerated()
        );
    }
}
