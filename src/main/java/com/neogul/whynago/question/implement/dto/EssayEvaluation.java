package com.neogul.whynago.question.implement.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.domain.SolvingTime;
import java.util.List;

// score는 AI가 항상 산출하므로 int다. 저장 경로에서 클라이언트가 중계하지 않았을 때만 null이 된다.
// mastery·masteryReason은 AI가 판정하지 못했으면 null이며, 그때는 숙련도를 기록하지 않는다.
// rubricCriteria는 루브릭이 적용된 턴에만 채워지고, 그 외에는 빈 리스트다.
// solvingTime은 점수에 얼마가 가감됐는지를 클라이언트가 설명할 수 있게 함께 내보낸다.
// turn은 서술형 대화의 몇 번째 턴인지다(1 = 본질문). 서버가 대화 이력에서 세며,
// 클라이언트가 보고하지 않는다 — 턴을 클라이언트에 맡기면 판정 출처를 조작할 수 있다.
public record EssayEvaluation(
        String feedback,
        String modelAnswer,
        int score,
        boolean isCorrect,
        String followupQuestion,
        MasteryLevel mastery,
        String masteryReason,
        List<RubricEvaluation> rubricCriteria,
        SolvingTime solvingTime,
        int turn
) {

    private static final int FIRST_TURN = 1;

    // 본질문 턴인지다. 꼬리질문 판정은 태그별 현재 숙련도를 덮어쓰지 않는다.
    public boolean isRootTurn() {
        return turn <= FIRST_TURN;
    }

    public boolean hasMastery() {
        return mastery != null;
    }

    public List<RubricEvaluation> rubricCriteria() {
        return rubricCriteria == null ? List.of() : rubricCriteria;
    }

    public SolvingTime solvingTime() {
        return solvingTime == null ? SolvingTime.unmeasured() : solvingTime;
    }
}
