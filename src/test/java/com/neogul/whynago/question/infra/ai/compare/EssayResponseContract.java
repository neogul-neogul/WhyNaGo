package com.neogul.whynago.question.infra.ai.compare;

import org.assertj.core.api.SoftAssertions;

/**
 * 모델이 무엇을 답하든 서비스가 기대하는 응답 계약은 지켜져야 한다.
 * 한 번만 호출하든 여러 모델을 비교하든 확인할 내용이 같아 한 곳에 모아 둔다.
 */
public class EssayResponseContract {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;

    public static void assertSatisfied(SoftAssertions softly, ModelScenarioResult result) {
        String label = "%s/%s(시도 %d)".formatted(result.model(), result.scenario().name(), result.attempt());

        softly.assertThat(result.failure())
                .as(label + " 호출 실패")
                .isNull();
        softly.assertThat(result.turns())
                .as(label + " 응답 턴")
                .isNotEmpty();

        for (EssayTurnResponse turn : result.turns()) {
            assertTurn(softly, "%s 턴 %d".formatted(label, turn.turn()), turn);
        }
    }

    private static void assertTurn(SoftAssertions softly, String label, EssayTurnResponse turn) {
        softly.assertThat(turn.result().feedback())
                .as(label + " feedback")
                .isNotBlank();
        softly.assertThat(turn.result().modelAnswer())
                .as(label + " modelAnswer")
                .isNotBlank();
        softly.assertThat(turn.result().score())
                .as(label + " score")
                .isBetween(MIN_SCORE, MAX_SCORE);

        if (turn.followupRequested()) {
            softly.assertThat(turn.result().followupQuestion())
                    .as(label + " followupQuestion")
                    .isNotBlank();
            return;
        }
        softly.assertThat(turn.result().followupQuestion())
                .as(label + " 꼬리질문을 요청하지 않은 턴의 followupQuestion")
                .isNull();
    }
}
