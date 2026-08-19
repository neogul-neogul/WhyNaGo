package com.neogul.whynago.solvedsession.infra.dto;

public interface QuestionSolveCount {

    Long getQuestionId();

    long getTotalCount();

    // 집계 대상 행이 없으면 sum()이 null을 반환한다.
    Long getCorrectCount();
}
