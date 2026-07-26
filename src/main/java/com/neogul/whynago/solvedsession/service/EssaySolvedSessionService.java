package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySessionAppender;
import com.neogul.whynago.solvedsession.implement.EssaySolvedAppender;
import com.neogul.whynago.solvedsession.implement.EssaySolvedSessionValidator;
import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;
import com.neogul.whynago.solvedsession.implement.dto.GradedEssayQuestions;
import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionCommand;
import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionResult;
import com.neogul.whynago.wrongnote.implement.WrongNoteAppender;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EssaySolvedSessionService {

    private final EssaySolvedSessionValidator essaySolvedSessionValidator;
    private final EssaySessionAppender essaySessionAppender;
    private final EssaySolvedAppender essaySolvedAppender;
    private final WrongNoteAppender wrongNoteAppender;

    @Transactional
    public CreateEssaySolvedSessionResult create(Long userId, CreateEssaySolvedSessionCommand command) {
        essaySolvedSessionValidator.validate(command.rootQuestionId());

        List<EssaySolvedPayload> payloads = command.toPayloads();
        GradedEssayQuestions graded = GradedEssayQuestions.from(payloads);
        LocalDateTime solvedAt = LocalDateTime.now();

        SolvedSession session = essaySessionAppender.append(
                userId,
                graded.totalCount(),
                graded.correctCount(),
                solvedAt
        );
        essaySolvedAppender.appendAll(userId, session.getId(), graded.items(), solvedAt);
        wrongNoteAppender.appendIfWrongAnswer(userId, session.getId(), graded.hasWrongAnswer());

        return CreateEssaySolvedSessionResult.from(session);
    }
}
