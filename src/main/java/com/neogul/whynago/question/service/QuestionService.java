package com.neogul.whynago.question.service;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.AnswerChoiceValidator;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.dto.QuestionPage;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
import com.neogul.whynago.question.service.dto.QuestionResult;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import com.neogul.whynago.question.service.dto.QuestionsResult;
import com.neogul.whynago.solvedsession.implement.SolvedQuestionIdReader;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final SolvedQuestionIdReader solvedQuestionIdReader;

    public QuestionsResult findQuestions(Long userId, QuestionSearchCommand command) {
        QuestionPage questionPage = questionReader.readQuestionPage(
                command.type(),
                command.difficulty(),
                command.category(),
                command.keyword(),
                command.page(),
                command.size()
        );
        List<Question> questions = questionPage.questions();
        Map<Long, List<String>> tagsByQuestionId = questionReader.readTagNames(questions.stream()
                .map(Question::getId)
                .toList());
        Set<Long> solvedQuestionIds = readSolvedQuestionIds(userId);

        List<QuestionResult> results = questions.stream()
                .map(question -> QuestionResult.from(
                        question,
                        readChoices(question),
                        tagsByQuestionId.getOrDefault(question.getId(), List.of()),
                        solvedQuestionIds.contains(question.getId())
                ))
                .toList();

        return new QuestionsResult(results, command.page(), command.size(), questionPage.totalElements());
    }

    public QuestionResult findQuestion(Long userId, Long questionId) {
        Question question = questionReader.read(questionId);

        return QuestionResult.from(
                question,
                readChoices(question),
                questionReader.readTagNames(List.of(question.getId()))
                        .getOrDefault(question.getId(), List.of()),
                readSolvedQuestionIds(userId).contains(question.getId())
        );
    }

    // 비로그인은 푼 문제를 조회하지 않는다.
    private Set<Long> readSolvedQuestionIds(Long userId) {
        if (userId == null) {
            return Set.of();
        }
        return Set.copyOf(solvedQuestionIdReader.readAll(userId));
    }

    // 서술형은 선택지가 없으므로 조회하지 않는다.
    private List<ChoiceResult> readChoices(Question question) {
        if (question.isEssay()) {
            return List.of();
        }
        return answerChoiceReader.readChoices(question.getId()).stream()
                .map(ChoiceResult::from)
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

    // 채점 흐름에서는 완료 표시를 쓰지 않으므로 solved는 항상 false다.
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
                        .getOrDefault(nextQuestion.getId(), List.of()),
                false
        );
    }

    public EssayQuestionResult findEssayQuestion(Long questionId) {
        Question question = questionReader.readEssayQuestion(questionId);
        List<String> tags = questionReader.readTagNames(List.of(question.getId()))
                .getOrDefault(question.getId(), List.of());

        return EssayQuestionResult.from(question, tags);
    }
}
