package com.neogul.whynago.question.domain;

// 문항의 노출 게이트다. QuestionSource(생성 시점에 확정되는 불변 출신)와 축을 나눈 이유는
// 승인된 뒤에도 "AI가 만든 문항인가"를 물어야 하기 때문이다(추천 자기 참조·통계 오염 대응).
//
// APPROVED: 검수를 통과한 문항. 문제은행 목록·오늘의 면접 질문·진척도 분모에 모두 포함된다.
// PENDING:  AI가 생성해 저장했지만 검수 전인 문항. 생성 요청자의 추천 응답과 단건 조회로만 도달한다.
// REJECTED: 검수에서 거절된 문항. 풀이 이력 FK 때문에 삭제하지 않고 상태로만 남긴다.
public enum QuestionReviewStatus {
    APPROVED,
    PENDING,
    REJECTED,
}
