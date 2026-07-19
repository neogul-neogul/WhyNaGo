package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.AnswerChoice;

public final class AnswerChoiceFixture {

    private AnswerChoiceFixture() {
    }

    public static AnswerChoice correct(Long questionId, int sequence, Long relatedQuestionId) {
        return AnswerChoice.create(
                questionId,
                "정답 선택지",
                sequence,
                true,
                "",
                relatedQuestionId
        );
    }

    public static AnswerChoice wrong(Long questionId, int sequence) {
        return AnswerChoice.create(
                questionId,
                "오답 선택지 " + sequence,
                sequence,
                false,
                "오답 사유",
                null
        );
    }
}
