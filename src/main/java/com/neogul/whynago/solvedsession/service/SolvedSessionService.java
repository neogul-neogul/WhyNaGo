package com.neogul.whynago.solvedsession.service;

import com.neogul.whynago.question.implement.ChoiceMasteryRecorder;
import com.neogul.whynago.question.implement.dto.ChoiceSolveSignal;
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
    private final ChoiceMasteryRecorder choiceMasteryRecorder;

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
                command.startedAt(),
                solvedAt
        );
        solvedMultipleChoiceAppender.appendAll(userId, savedSession.getId(), scoredQuestions.items(), solvedAt);
        wrongNoteAppender.appendIfWrongAnswer(userId, savedSession.getId(), scoredQuestions.hasWrongAnswer());
        // 숙련도는 여기서 기록한다. 서버가 다시 채점한 결과를 쓰므로 클라이언트가 정답 여부를 조작할 수 없다.
        // 꼬리질문도 각자 문항이고 태그가 붙으므로 본질문과 똑같이 기록한다.
        choiceMasteryRecorder.recordAll(userId, scoredQuestions.items().stream()
                .map(item -> new ChoiceSolveSignal(item.questionId(), item.correct(), item.elapsedSeconds()))
                .toList());

        return CreateSolvedSessionResult.from(savedSession);
    }
}
