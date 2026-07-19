package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;

public final class QuestionFixture {

    private QuestionFixture() {
    }

    public static Question rootMultipleChoice() {
        return Question.create(
                "TCP와 UDP의 핵심 차이",
                "TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.NETWORK,
                "TCP는 연결 지향형이고 UDP는 비연결형이다."
        );
    }

    public static Question followupMultipleChoice() {
        return Question.create(
                "실시간 음성 통화와 UDP",
                "실시간 음성 통화에 UDP가 적합한 가장 큰 이유는?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.NETWORK,
                "낮은 지연이 중요하다."
        );
    }

    public static Question essayRoot() {
        return Question.create(
                "트랜잭션 격리 수준 설명",
                "트랜잭션 격리 수준을 설명하라.",
                QuestionType.ESSAY,
                Difficulty.HIGH,
                Category.DB,
                "격리 수준별 이상 현상이 다르다."
        );
    }
}
