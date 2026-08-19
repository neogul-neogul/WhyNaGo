package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

public record AdminQuestionDetailResult(
        Long id,
        String title,
        String content,
        QuestionType type,
        Difficulty difficulty,
        Category category,
        String explanation,
        List<AdminChoiceResult> choices,
        List<String> tags,
        // 서술형만 값이 있다. 객관식 통계는 별도 통계 조회 API(GET .../statistics)를 쓴다.
        Long solveCount,
        Double correctRate
) {

    public static AdminQuestionDetailResult of(
            Question question,
            List<AdminChoiceResult> choices,
            List<String> tags,
            Long solveCount,
            Double correctRate
    ) {
        return new AdminQuestionDetailResult(
                question.getId(),
                question.getTitle(),
                question.getContent(),
                question.getType(),
                question.getDifficulty(),
                question.getCategory(),
                question.getExplanation(),
                choices,
                tags,
                solveCount,
                correctRate
        );
    }
}
