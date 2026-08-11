package com.neogul.whynago.solvedsession.service.dto;

import java.util.List;

public record SolvedQuestionIdsResult(List<Long> questionIds) {

    public static SolvedQuestionIdsResult from(List<Long> questionIds) {
        return new SolvedQuestionIdsResult(questionIds);
    }
}