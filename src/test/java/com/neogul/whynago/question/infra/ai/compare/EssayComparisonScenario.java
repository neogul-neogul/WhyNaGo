package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.domain.EssayGradingMode;
import java.util.List;

/**
 * 모델 비교에 쓰는 문답 시나리오.
 * answers가 둘 이상이면 앞 턴이 만든 꼬리질문을 다음 턴의 질문으로 이어 붙여 멀티턴으로 진행한다.
 */
public record EssayComparisonScenario(
        String name,
        EssayGradingMode mode,
        String question,
        List<String> answers,
        boolean requestFollowupOnLastTurn
) {

    public int turnCount() {
        return answers.size();
    }

    // 마지막 턴이 아니면 다음 턴의 질문이 필요하므로 항상 꼬리질문을 요청한다.
    public boolean generateFollowupAt(int turnIndex) {
        return turnIndex < answers.size() - 1 || requestFollowupOnLastTurn;
    }
}
