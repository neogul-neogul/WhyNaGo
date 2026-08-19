package com.neogul.whynago.solvedsession.implement.dto;

import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.solvedsession.infra.dto.TypeSolveCount;
import java.util.List;

public record SolveCountByType(long multipleChoiceCount, long essayCount, long totalCount) {

    public static SolveCountByType from(List<TypeSolveCount> counts) {
        long multipleChoiceCount = countOf(counts, QuestionType.MULTIPLE_CHOICE);
        long essayCount = countOf(counts, QuestionType.ESSAY);
        return new SolveCountByType(multipleChoiceCount, essayCount, multipleChoiceCount + essayCount);
    }

    private static long countOf(List<TypeSolveCount> counts, QuestionType type) {
        return counts.stream()
                .filter(count -> count.getType() == type)
                .mapToLong(TypeSolveCount::getQuestionCount)
                .findFirst()
                .orElse(0L);
    }
}
