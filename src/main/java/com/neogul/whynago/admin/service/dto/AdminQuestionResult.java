package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionReviewStatus;
import com.neogul.whynago.question.domain.QuestionSource;
import com.neogul.whynago.question.domain.QuestionType;

public record AdminQuestionResult(
        Long id,
        String title,
        Category category,
        Difficulty difficulty,
        QuestionType type,
        // 관리자 목록은 검수 전 문항까지 보여주므로, 어떤 상태인지·누가 만들었는지를 함께 내려
        // 화면이 구분할 수 있게 한다. 사용자용 목록에는 두 값 모두 나가지 않는다.
        QuestionReviewStatus reviewStatus,
        QuestionSource source,
        long solveCount,
        // 풀이가 한 건도 없으면 null이다. 0%와 "아직 안 풀림"은 다르다.
        Double correctRate
) {

    public static AdminQuestionResult of(Question question, long solveCount, Double correctRate) {
        return new AdminQuestionResult(
                question.getId(),
                question.getTitle(),
                question.getCategory(),
                question.getDifficulty(),
                question.getType(),
                question.getReviewStatus(),
                question.getSource(),
                solveCount,
                correctRate
        );
    }
}
