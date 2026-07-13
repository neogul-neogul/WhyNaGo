package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SessionSource;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceAppender;
import com.neogul.whynago.solvedsession.implement.SolvedSessionAppender;
import com.neogul.whynago.wrongnote.implement.WrongNoteAppender;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolvedSessionService {

    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;
    private final SolvedSessionAppender solvedSessionAppender;
    private final SolvedMultipleChoiceAppender solvedMultipleChoiceAppender;
    private final WrongNoteAppender wrongNoteAppender;

    @Transactional
    public SubmitSessionResult submit(Long userId, SubmitSessionCommand command) {
        Question rootQuestion = questionReader.read(command.rootQuestionId());
        validateRootQuestion(rootQuestion);
        validateFirstAnswerIsRoot(command);

        LocalDateTime solvedAt = LocalDateTime.now();
        List<ScoredAnswer> scoredAnswers = scoreAnswers(command.answers());
        int totalCount = scoredAnswers.size();
        int correctCount = (int) scoredAnswers.stream()
                .filter(ScoredAnswer::correct)
                .count();

        SolvedSession solvedSession = createSession(userId, command, totalCount, correctCount, solvedAt);
        SolvedSession savedSession = solvedSessionAppender.append(solvedSession);

        List<SolvedMultipleChoice> items = createItems(userId, savedSession.getId(), scoredAnswers, solvedAt);
        solvedMultipleChoiceAppender.appendAll(items);

        if (correctCount < totalCount) {
            wrongNoteAppender.appendIfAbsent(userId, savedSession.getId());
        }

        return SubmitSessionResult.of(savedSession, totalCount, correctCount, scoredAnswers);
    }

    private void validateRootQuestion(Question rootQuestion) {
        if (!rootQuestion.isRoot() || rootQuestion.getType() != QuestionType.MULTIPLE_CHOICE) {
            throw new BusinessException(QuestionErrorCode.QUESTION_NOT_ROOT);
        }
    }

    private void validateFirstAnswerIsRoot(SubmitSessionCommand command) {
        Long firstQuestionId = command.answers().getFirst().questionId();
        if (!command.rootQuestionId().equals(firstQuestionId)) {
            throw new BusinessException(QuestionErrorCode.QUESTION_NOT_ROOT);
        }
    }

    private List<ScoredAnswer> scoreAnswers(List<SubmittedAnswer> answers) {
        List<ScoredAnswer> scoredAnswers = new ArrayList<>();
        for (SubmittedAnswer answer : answers) {
            AnswerChoice chosen = answerChoiceReader.read(answer.choiceId());
            answerChoiceReader.validateChoiceInQuestion(chosen, answer.questionId());
            AnswerChoice correctChoice = answerChoiceReader.readCorrectChoice(answer.questionId());
            scoredAnswers.add(new ScoredAnswer(
                    answer.questionId(),
                    chosen.getId(),
                    correctChoice.getId(),
                    chosen.correct()
            ));
        }
        return scoredAnswers;
    }

    private SolvedSession createSession(
            Long userId,
            SubmitSessionCommand command,
            int totalCount,
            int correctCount,
            LocalDateTime solvedAt
    ) {
        if (command.completed()) {
            return SolvedSession.completed(
                    userId,
                    QuestionType.MULTIPLE_CHOICE,
                    command.source(),
                    totalCount,
                    correctCount,
                    solvedAt
            );
        }
        return SolvedSession.abandoned(
                userId,
                QuestionType.MULTIPLE_CHOICE,
                command.source(),
                totalCount,
                correctCount,
                solvedAt
        );
    }

    private List<SolvedMultipleChoice> createItems(
            Long userId,
            Long solvedSessionId,
            List<ScoredAnswer> scoredAnswers,
            LocalDateTime solvedAt
    ) {
        List<SolvedMultipleChoice> items = new ArrayList<>();
        for (int i = 0; i < scoredAnswers.size(); i++) {
            ScoredAnswer answer = scoredAnswers.get(i);
            items.add(SolvedMultipleChoice.create(
                    solvedSessionId,
                    userId,
                    answer.questionId(),
                    i == 0 ? ItemType.MAIN : ItemType.FOLLOWUP,
                    i,
                    answer.userChoiceId(),
                    answer.correctChoiceId(),
                    answer.correct(),
                    solvedAt
            ));
        }
        return items;
    }

    public record SubmitSessionCommand(
            Long rootQuestionId,
            SessionSource source,
            boolean completed,
            List<SubmittedAnswer> answers
    ) {
    }

    public record SubmittedAnswer(Long questionId, Long choiceId) {
    }

    public record SubmitSessionResult(
            Long sessionId,
            int totalCount,
            int correctCount,
            int wrongCount,
            double correctRate,
            SessionStatus status,
            List<ScoredAnswer> items
    ) {

        static SubmitSessionResult of(
                SolvedSession solvedSession,
                int totalCount,
                int correctCount,
                List<ScoredAnswer> items
        ) {
            int wrongCount = totalCount - correctCount;
            double correctRate = totalCount == 0 ? 0.0 : correctCount * 100.0 / totalCount;
            return new SubmitSessionResult(
                    solvedSession.getId(),
                    totalCount,
                    correctCount,
                    wrongCount,
                    correctRate,
                    solvedSession.getStatus(),
                    items
            );
        }
    }

    public record ScoredAnswer(
            Long questionId,
            Long userChoiceId,
            Long correctChoiceId,
            boolean correct
    ) {
    }
}
