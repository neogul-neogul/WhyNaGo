package com.neogul.whynago.common.domain;

// 문항 1건에 대한 숙련도다. 서술형은 채점 AI가 답변 내용을 근거로 직접 판정하고,
// 객관식은 정답 여부와 소요시간 비율을 교차해 서버가 판정한다(mastery의 MasteryPolicy).
//
// 두 판정 주체가 같은 분류를 쓰므로 특정 도메인에 두지 않는다.
// 약점 가중치는 추천의 개념이라 여기 두지 않고 recommendation.domain의 정책이 갖는다.
public enum MasteryLevel {

    // 빠르게·정확하게 맞혔다. 더 낼 이유가 없다.
    MASTERED,
    // 필요한 근거를 갖춰 맞혔다.
    SOLID,
    // 결론은 맞지만 근거가 흔들린다. 알지만 헤맨 상태다.
    UNSTABLE,
    // 근거 없이 용어만 맞혔다. 찍었을 가능성이 크다.
    GUESSED,
    // 개념을 알지만 틀렸다. 전형적인 오개념이다.
    WEAK,
    // 개념 자체가 없다.
    NOT_LEARNED,
}
