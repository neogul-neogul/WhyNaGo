package com.neogul.whynago.common.domain;

// 문제 평균 소요시간 대비 이번 풀이가 빨랐는지 느렸는지다.
//
// 객관식 숙련도 판정(mastery의 MasteryPolicy)과 서술형 채점(question)이 같은 기준을 써야 하므로
// 특정 도메인에 두지 않는다. 두 트랙이 다른 임계값을 쓰면 같은 소요시간이 화면마다 다르게 해석된다.
public enum ElapsedPace {

    // 평균보다 뚜렷하게 빠르다.
    FAST,
    // 평균 수준이다. 시간을 측정하지 못했을 때도 여기로 본다.
    NORMAL,
    // 평균보다 뚜렷하게 오래 걸렸다.
    SLOW,
}
