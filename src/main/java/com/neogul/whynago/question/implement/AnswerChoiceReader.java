package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerChoiceReader {

    private final AnswerChoiceRepository answerChoiceRepository;

    public AnswerChoice read(Long choiceId) {
        return answerChoiceRepository.findById(choiceId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.CHOICE_NOT_FOUND));
    }

    public AnswerChoice readCorrectChoice(Long questionId) {
        return answerChoiceRepository.findFirstByQuestionIdAndIsCorrectTrue(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.CHOICE_NOT_FOUND));
    }

    public List<AnswerChoice> readChoices(Long questionId) {
        return answerChoiceRepository.findByQuestionIdOrderBySequence(questionId);
    }
}
