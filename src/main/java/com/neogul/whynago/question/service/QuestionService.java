package com.neogul.whynago.question.service;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.AnswerChoiceValidator;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.QuestionResult;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;
    private final AnswerChoiceValidator answerChoiceValidator;

    public List<QuestionResult> findQuestions(QuestionSearchCommand command) {
        List<Question> questions = questionReader.readRootMultipleChoices(
                command.type(),
                command.difficulty(),
                command.category(),
                command.keyword()
        );
        Map<Long, List<String>> tagsByQuestionId = questionReader.readTagNames(questions.stream()
                .map(Question::getId)
                .toList());

        return questions.stream()
                .map(question -> QuestionResult.from(
                        question,
                        answerChoiceReader.readChoices(question.getId()).stream()
                                .map(ChoiceResult::from)
                                .toList(),
                        tagsByQuestionId.getOrDefault(question.getId(), List.of())
                ))
                .toList();
    }

    public ChoiceGradingResult getChoiceGrading(Long questionId, Long choiceId) {
        Question question = questionReader.read(questionId);
        AnswerChoice chosenChoice = answerChoiceReader.read(choiceId);
        answerChoiceValidator.validateChoiceInQuestion(chosenChoice, question.getId());
        AnswerChoice correctChoice = answerChoiceReader.readCorrectChoice(question.getId());
        QuestionResult nextQuestion = readNextQuestion(chosenChoice.nextQuestionId());

        return ChoiceGradingResult.of(question, chosenChoice, correctChoice, nextQuestion);
    }

    private QuestionResult readNextQuestion(Long nextQuestionId) {
        if (nextQuestionId == null) {
            return null;
        }
        Question nextQuestion = questionReader.read(nextQuestionId);
        return QuestionResult.from(
                nextQuestion,
                answerChoiceReader.readChoices(nextQuestion.getId()).stream()
                        .map(ChoiceResult::from)
                        .toList(),
                questionReader.readTagNames(List.of(nextQuestion.getId()))
                        .getOrDefault(nextQuestion.getId(), List.of())
        );
    }
}
