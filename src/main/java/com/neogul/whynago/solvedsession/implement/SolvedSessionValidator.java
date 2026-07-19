package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.AnswerChoiceValidator;
import com.neogul.whynago.solvedsession.exception.SolvedSessionErrorCode;
import com.neogul.whynago.solvedsession.implement.dto.SolvedQuestionPayload;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedSessionValidator {

    private final AnswerChoiceReader answerChoiceReader;
    private final AnswerChoiceValidator answerChoiceValidator;

    public void validate(List<SolvedQuestionPayload> questions) {
        for (int i = 0; i < questions.size(); i++) {
            SolvedQuestionPayload current = questions.get(i);
            AnswerChoice userChoice = answerChoiceReader.read(current.choiceId());
            answerChoiceValidator.validateChoiceInQuestion(userChoice, current.questionId());
            validateChain(current, userChoice, nextQuestionId(questions, i));
        }
    }

    private void validateChain(SolvedQuestionPayload question, AnswerChoice userChoice, Long nextQuestionId) {
        if (!Objects.equals(userChoice.nextQuestionId(), question.relationQuestionId())
                || !Objects.equals(question.relationQuestionId(), nextQuestionId)) {
            throw new BusinessException(SolvedSessionErrorCode.SOLVED_SESSION_BROKEN_CHAIN);
        }
    }

    private Long nextQuestionId(List<SolvedQuestionPayload> questions, int index) {
        if (index == questions.size() - 1) {
            return null;
        }
        return questions.get(index + 1).questionId();
    }
}
