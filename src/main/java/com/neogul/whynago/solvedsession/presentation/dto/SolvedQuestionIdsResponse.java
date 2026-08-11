package com.neogul.whynago.solvedsession.presentation.dto;

import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionIdsResult;
import java.util.List;

public record SolvedQuestionIdsResponse(List<Long> questionIds) {

    public static SolvedQuestionIdsResponse from(SolvedQuestionIdsResult result) {
        return new SolvedQuestionIdsResponse(result.questionIds());
    }
}