package com.neogul.whynago.solvedsession.infra.dto;

public interface QuestionSolveSummary {

    long getTotalCount();

    // 집계 대상 행이 없으면 sum()이 null을 반환한다.
    Long getCorrectCount();

    // 소요 시간이 수집된 응답이 하나도 없으면 avg()가 null을 반환한다.
    Double getAverageElapsedSeconds();

    long getElapsedSampleCount();
}
