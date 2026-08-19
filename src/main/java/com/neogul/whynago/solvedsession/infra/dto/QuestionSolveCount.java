package com.neogul.whynago.solvedsession.infra.dto;

// 문항별 풀이 집계 프로젝션. avgElapsedSeconds는 소요 시간을 보고한 표본만 평균하며,
// 그런 표본이 하나도 없으면 null이다.
public interface QuestionSolveCount {

    Long getQuestionId();

    long getSolvedCount();

    long getCorrectCount();

    Double getAvgElapsedSeconds();
}
