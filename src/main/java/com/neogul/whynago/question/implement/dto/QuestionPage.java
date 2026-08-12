package com.neogul.whynago.question.implement.dto;

import com.neogul.whynago.question.domain.Question;
import java.util.List;

public record QuestionPage(
        List<Question> questions,
        long totalElements
) {
}
