package com.neogul.whynago.interview.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;
import java.util.ArrayList;
import java.util.List;

public record CompleteInterviewCommand(
        InterviewAnswerSnapshotCommand rootQuestion,
        List<InterviewAnswerSnapshotCommand> followupQuestions,
        int focusLossCount
) {

    public List<EssaySolvedPayload> toPayloads() {
        List<InterviewAnswerSnapshotCommand> ordered = new ArrayList<>();
        ordered.add(rootQuestion);
        ordered.addAll(followupQuestions);
        return ordered.stream()
                .map(InterviewAnswerSnapshotCommand::toPayload)
                .toList();
    }
}
