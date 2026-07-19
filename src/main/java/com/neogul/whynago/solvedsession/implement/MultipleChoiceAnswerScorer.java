package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.solvedsession.implement.dto.ScoredQuestion;
import com.neogul.whynago.solvedsession.implement.dto.ScoredQuestions;
import com.neogul.whynago.solvedsession.implement.dto.SolvedQuestionPayload;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MultipleChoiceAnswerScorer {

    private final AnswerChoiceReader answerChoiceReader;

    public ScoredQuestions score(List<SolvedQuestionPayload> questions) {
        return ScoredQuestions.from(questions.stream()
                .map(this::score)
                .toList());
    }

    private ScoredQuestion score(SolvedQuestionPayload question) {
        AnswerChoice userChoice = answerChoiceReader.read(question.choiceId());
        AnswerChoice correctChoice = answerChoiceReader.readCorrectChoice(question.questionId());
        return new ScoredQuestion(
                question.questionId(),
                userChoice.getId(),
                correctChoice.getId(),
                userChoice.correct()
        );
    }
}
