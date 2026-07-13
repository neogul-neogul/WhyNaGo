package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionReader {

    private final QuestionRepository questionRepository;
    private final QuestionTagRepository questionTagRepository;

    public Question read(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    public List<Question> readRootMultipleChoices(
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String keyword
    ) {
        return questionRepository.findRootMultipleChoices(type, difficulty, category, normalize(keyword));
    }

    public List<QuestionTag> readTags(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return List.of();
        }
        return questionTagRepository.findByQuestionIdIn(questionIds);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
