package com.neogul.whynago.mastery.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.question.domain.Category;
import java.util.List;

// mastery 도메인은 question 저장소를 참조하지 않는다. 판정을 태그·카테고리에 연결하는 데 필요한 값은
// 호출자(채점 흐름)가 채워 넘긴다. tagIds가 비면 카테고리 단위 신호로만 기록된다.
//
// turn은 서술형 대화의 몇 번째 턴인지다(1 = 본질문). 객관식은 턴 개념이 없어 null이고,
// 서술형이라도 판정 시점의 턴을 알 수 없었던 과거 이력은 null이다 — null은 "본질문"이 아니라 "미지"다.
public record RecordMasteryCommand(
        Long userId,
        Long questionId,
        Category category,
        List<Long> tagIds,
        MasteryLevel level,
        String reason,
        MasterySource source,
        Integer turn
) {

    private static final int FIRST_TURN = 1;

    // 서술형. 턴이 곧 판정 출처를 가른다 — 호출부가 source와 turn을 따로 정하다가 어긋나지 않게 한 곳에서 묶는다.
    public static RecordMasteryCommand ofEssay(
            Long userId,
            Long questionId,
            Category category,
            List<Long> tagIds,
            MasteryLevel level,
            String reason,
            int turn
    ) {
        MasterySource source = turn <= FIRST_TURN ? MasterySource.AI_ESSAY : MasterySource.AI_ESSAY_FOLLOWUP;
        return new RecordMasteryCommand(userId, questionId, category, tagIds, level, reason, source, turn);
    }

    // 객관식. 꼬리질문도 그 자체가 Question이라 턴으로 묶이지 않는다.
    public static RecordMasteryCommand ofChoice(
            Long userId,
            Long questionId,
            Category category,
            List<Long> tagIds,
            MasteryLevel level,
            String reason
    ) {
        return new RecordMasteryCommand(
                userId, questionId, category, tagIds, level, reason, MasterySource.RULE_CHOICE, null);
    }
}
