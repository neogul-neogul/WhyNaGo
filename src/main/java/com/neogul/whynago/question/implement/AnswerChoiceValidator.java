package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import org.springframework.stereotype.Component;

@Component
public class AnswerChoiceValidator {

    public void validateChoiceInQuestion(AnswerChoice choice, Long questionId) {
        if (!choice.getQuestionId().equals(questionId)) {
            throw new BusinessException(QuestionErrorCode.CHOICE_NOT_IN_QUESTION);
        }
    }
}
