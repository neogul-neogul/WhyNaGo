package com.neogul.whynago.fixture;

import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import java.time.LocalDateTime;

// 필드가 많고 테스트마다 중요한 값이 달라 빌더로 둔다.
// 테스트가 신경 쓰는 값만 체이닝하고 나머지는 기본값에 맡긴다.
public final class SolvedMultipleChoiceFixture {

    private static final LocalDateTime DEFAULT_SOLVED_AT = LocalDateTime.of(2026, 8, 7, 9, 30);

    private Long solvedSessionId = 1L;
    private Long userId = 10L;
    private Long questionId = 100L;
    private ItemType type = ItemType.MAIN;
    private int sequence = 1;
    private Long userChoiceId = 1L;
    private Long answerChoiceId = 1L;
    private boolean isCorrect = true;
    private LocalDateTime solvedAt = DEFAULT_SOLVED_AT;

    private SolvedMultipleChoiceFixture() {
    }

    public static SolvedMultipleChoiceFixture builder() {
        return new SolvedMultipleChoiceFixture();
    }

    public SolvedMultipleChoiceFixture solvedSessionId(Long solvedSessionId) {
        this.solvedSessionId = solvedSessionId;
        return this;
    }

    public SolvedMultipleChoiceFixture userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public SolvedMultipleChoiceFixture questionId(Long questionId) {
        this.questionId = questionId;
        return this;
    }

    public SolvedMultipleChoiceFixture type(ItemType type) {
        this.type = type;
        return this;
    }

    public SolvedMultipleChoiceFixture sequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public SolvedMultipleChoiceFixture userChoiceId(Long userChoiceId) {
        this.userChoiceId = userChoiceId;
        return this;
    }

    public SolvedMultipleChoiceFixture answerChoiceId(Long answerChoiceId) {
        this.answerChoiceId = answerChoiceId;
        return this;
    }

    public SolvedMultipleChoiceFixture isCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
        return this;
    }

    public SolvedMultipleChoiceFixture solvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
        return this;
    }

    public SolvedMultipleChoice build() {
        return SolvedMultipleChoice.create(
                solvedSessionId,
                userId,
                questionId,
                type,
                sequence,
                userChoiceId,
                answerChoiceId,
                isCorrect,
                solvedAt
        );
    }
}
