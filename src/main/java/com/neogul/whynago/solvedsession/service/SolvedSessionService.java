package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.MultipleChoiceAnswerScorer;
import com.neogul.whynago.solvedsession.implement.dto.ScoredQuestions;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceAppender;
import com.neogul.whynago.solvedsession.implement.dto.SolvedQuestionPayload;
import com.neogul.whynago.solvedsession.implement.SolvedSessionAppender;
import com.neogul.whynago.solvedsession.implement.SolvedSessionValidator;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionCommand;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionResult;
import com.neogul.whynago.wrongnote.implement.WrongNoteAppender;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SolvedSessionService {

    private final SolvedSessionValidator solvedSessionValidator;
    private final MultipleChoiceAnswerScorer multipleChoiceAnswerScorer;
    private final SolvedSessionAppender solvedSessionAppender;
    private final SolvedMultipleChoiceAppender solvedMultipleChoiceAppender;
    private final WrongNoteAppender wrongNoteAppender;

    @Transactional
    public CreateSolvedSessionResult create(Long userId, CreateSolvedSessionCommand command) {
        List<SolvedQuestionPayload> solvedQuestions = command.toPayloads();
        solvedSessionValidator.validate(solvedQuestions);

        ScoredQuestions scoredQuestions = multipleChoiceAnswerScorer.score(solvedQuestions);
        LocalDateTime solvedAt = LocalDateTime.now();

        SolvedSession savedSession = solvedSessionAppender.append(
                userId,
                scoredQuestions.totalCount(),
                scoredQuestions.correctCount(),
                solvedAt
        );
        solvedMultipleChoiceAppender.appendAll(userId, savedSession.getId(), scoredQuestions.items(), solvedAt);
        wrongNoteAppender.appendIfWrongAnswer(userId, savedSession.getId(), scoredQuestions.hasWrongAnswer());

        return CreateSolvedSessionResult.from(savedSession);
    }
}
