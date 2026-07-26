package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.EssaySolvedPayload;
import java.util.ArrayList;
import java.util.List;

public record CreateEssaySolvedSessionCommand(
        EssaySolvedQuestionCommand rootQuestion,
        List<EssaySolvedQuestionCommand> followupQuestions
) {

    public List<EssaySolvedPayload> toPayloads() {
        List<EssaySolvedQuestionCommand> ordered = new ArrayList<>();
        ordered.add(rootQuestion);
        ordered.addAll(followupQuestions);
        return ordered.stream()
                .map(EssaySolvedQuestionCommand::toPayload)
                .toList();
    }

    public Long rootQuestionId() {
        return rootQuestion.questionId();
    }
}
