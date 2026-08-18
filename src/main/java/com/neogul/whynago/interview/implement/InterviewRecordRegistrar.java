package com.neogul.whynago.interview.implement;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySessionAppender;
import com.neogul.whynago.solvedsession.implement.EssaySolvedAppender;
import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;
import com.neogul.whynago.solvedsession.implement.dto.GradedEssayQuestions;
import com.neogul.whynago.wrongnote.implement.WrongNoteAppender;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 저장 흐름 자체는 EssaySolvedSessionService와 같지만, service 간 호출을 두지 않는 규칙에 따라
// 같은 implement(EssaySessionAppender·EssaySolvedAppender·WrongNoteAppender)를 재사용해 여기서 조립한다.
// 면접은 rootQuestionId(오늘의 질문)와 startedAt을 서버가 소유하므로 클라이언트 payload를 그대로 쓸 수 없다.
@Component
@RequiredArgsConstructor
public class InterviewRecordRegistrar {

    private final EssaySessionAppender essaySessionAppender;
    private final EssaySolvedAppender essaySolvedAppender;
    private final WrongNoteAppender wrongNoteAppender;

    public Long register(
            Long userId,
            Long rootQuestionId,
            List<EssaySolvedPayload> payloads,
            LocalDateTime startedAt,
            LocalDateTime completedAt
    ) {
        GradedEssayQuestions graded = GradedEssayQuestions.from(withRootQuestionId(rootQuestionId, payloads));

        SolvedSession session = essaySessionAppender.append(
                userId,
                graded.totalCount(),
                graded.correctCount(),
                startedAt,
                completedAt
        );
        essaySolvedAppender.appendAll(userId, session.getId(), graded.items(), completedAt);
        wrongNoteAppender.appendIfWrongAnswer(userId, session.getId(), graded.hasWrongAnswer());

        return session.getId();
    }

    private List<EssaySolvedPayload> withRootQuestionId(Long rootQuestionId, List<EssaySolvedPayload> payloads) {
        List<EssaySolvedPayload> resolved = new ArrayList<>();
        for (int i = 0; i < payloads.size(); i++) {
            EssaySolvedPayload payload = payloads.get(i);
            resolved.add(new EssaySolvedPayload(
                    i == 0 ? rootQuestionId : null,
                    payload.questionText(),
                    payload.userAnswer(),
                    payload.feedback(),
                    payload.modelAnswer(),
                    payload.isCorrect(),
                    payload.elapsedSeconds()
            ));
        }
        return resolved;
    }
}
