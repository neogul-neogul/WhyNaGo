package com.neogul.whynago.mastery.domain;

// 숙련도를 누가 판정했는지다. 서술형은 채점 AI가 답변 내용을 보고 판정하고,
// 객관식은 정답 여부와 소요시간 비율로 서버가 판정한다. 두 신호의 성격이 달라 구분해 남긴다.
//
// 서술형은 본질문과 꼬리질문을 다시 나눈다. 꼬리질문은 본질문보다 깊게 파고드는 프로브라
// 같은 태그라도 판정이 더 낮게 나오는 것이 정상이다. 한 축으로 합쳐 두면 마지막 프로브 판정이
// 그 태그의 현재 숙련도를 덮어써, 본질문을 제대로 답한 사용자가 그 주제를 모르는 것으로 기록된다.
public enum MasterySource {
    AI_ESSAY,
    AI_ESSAY_FOLLOWUP,
    RULE_CHOICE,
    ;

    // 꼬리질문 판정은 이력에만 남기고 태그별 현재값(user_tag_mastery)을 갱신하지 않는다.
    public boolean isFollowup() {
        return this == AI_ESSAY_FOLLOWUP;
    }
}
