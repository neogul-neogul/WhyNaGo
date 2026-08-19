package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.AnswerChoice;
import org.springframework.test.util.ReflectionTestUtils;

public final class AnswerChoiceFixture {

    private AnswerChoiceFixture() {
    }

    // 영속화하지 않는 단위 테스트에서 보기 식별자가 필요할 때만 주입한다.
    public static AnswerChoice withId(Long id, AnswerChoice choice) {
        ReflectionTestUtils.setField(choice, "id", id);
        return choice;
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
        return wrong(questionId, sequence, null);
    }

    public static AnswerChoice wrong(Long questionId, int sequence, Long relatedQuestionId) {
        return AnswerChoice.create(
                questionId,
                "오답 선택지 " + sequence,
                sequence,
                false,
                "오답 사유",
                relatedQuestionId
        );
    }
}
