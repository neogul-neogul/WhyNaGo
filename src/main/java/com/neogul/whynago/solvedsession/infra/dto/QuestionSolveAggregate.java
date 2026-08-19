package com.neogul.whynago.solvedsession.infra.dto;

// 야간 배치가 question_stat을 다시 만들 때 쓰는 전역 집계 프로젝션이다.
// 관리자 목록의 QuestionSolveCount와 나눠 둔 이유는, 이쪽만 평균 소요 시간을 필요로 하고
// 인터페이스 프로젝션은 접근자와 쿼리 별칭이 1:1로 맞아야 하기 때문이다.
//
// avgElapsedSeconds는 소요 시간을 보고한 표본만 평균하며, 그런 표본이 하나도 없으면 null이다.
public interface QuestionSolveAggregate {

    Long getQuestionId();

    long getSolvedCount();

    long getCorrectCount();

    Double getAvgElapsedSeconds();
}
