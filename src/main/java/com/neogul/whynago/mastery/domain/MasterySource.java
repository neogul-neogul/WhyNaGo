package com.neogul.whynago.mastery.domain;

// 숙련도를 누가 판정했는지다. 서술형은 채점 AI가 답변 내용을 보고 판정하고,
// 객관식은 정답 여부와 소요시간 비율로 서버가 판정한다. 두 신호의 성격이 달라 구분해 남긴다.
public enum MasterySource {
    AI_ESSAY,
    RULE_CHOICE,
}
