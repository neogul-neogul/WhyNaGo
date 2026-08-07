package com.neogul.whynago.interview.implement;

import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.implement.dto.InterviewResult;
import com.neogul.whynago.interview.implement.dto.InterviewResultItem;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InterviewResultAssembler {

    private final SolvedSessionReader solvedSessionReader;
    private final EssaySolvedReader essaySolvedReader;

    public InterviewResult assemble(DailyInterview interview) {
        SolvedSession session = solvedSessionReader.read(interview.getSolvedSessionId());
        List<InterviewResultItem> items = essaySolvedReader.readOrdered(session.getId()).stream()
                .map(InterviewResultItem::from)
                .toList();

        return new InterviewResult(
                interview.getId(),
                interview.getInterviewDate(),
                interview.getStatus(),
                interview.getCategory(),
                session.getTotalCount(),
                session.getCorrectCount(),
                interview.getFocusLossCount(),
                interview.getStartedAt(),
                interview.getCompletedAt(),
                Duration.between(interview.getStartedAt(), interview.getCompletedAt()).toSeconds(),
                items
        );
    }
}
