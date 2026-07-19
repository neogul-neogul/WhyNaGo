package com.neogul.whynago.solvedsession.service.dto;

import com.neogul.whynago.solvedsession.implement.dto.SolvedQuestionPayload;
import java.util.ArrayList;
import java.util.List;

public record CreateSolvedSessionCommand(
        SolvedQuestionCommand rootQuestion,
        List<SolvedQuestionCommand> followupQuestions
) {

    public List<SolvedQuestionPayload> toPayloads() {
        List<SolvedQuestionCommand> ordered = new ArrayList<>();
        ordered.add(rootQuestion);
        ordered.addAll(followupQuestions);
        return ordered.stream()
                .map(SolvedQuestionCommand::toPayload)
                .toList();
    }
}
