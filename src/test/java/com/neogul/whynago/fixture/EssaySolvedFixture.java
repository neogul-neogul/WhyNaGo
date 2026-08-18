package com.neogul.whynago.fixture;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import java.time.LocalDateTime;

// 필드가 많고 테스트마다 중요한 값이 달라 빌더로 둔다.
// 꼬리질문(FOLLOWUP)은 questionId가 null이라는 점이 테스트마다 중요해 명시적으로 넘기게 한다.
public final class EssaySolvedFixture {

    private static final LocalDateTime DEFAULT_SOLVED_AT = LocalDateTime.of(2026, 8, 7, 9, 30);

    private Long solvedSessionId = 1L;
    private Long userId = 10L;
    private ItemType type = ItemType.MAIN;
    private int sequence = 1;
    private Long questionId = 100L;
    private String questionText = "질문";
    private String userAnswer = "답변";
    private String feedback = "피드백";
    private String modelAnswer = "모범답안";
    private boolean isCorrect = true;
    private Integer elapsedSeconds = null;
    private LocalDateTime solvedAt = DEFAULT_SOLVED_AT;

    private EssaySolvedFixture() {
    }

    public static EssaySolvedFixture builder() {
        return new EssaySolvedFixture();
    }

    public EssaySolvedFixture solvedSessionId(Long solvedSessionId) {
        this.solvedSessionId = solvedSessionId;
        return this;
    }

    public EssaySolvedFixture userId(Long userId) {
        this.userId = userId;
        return this;
    }

    public EssaySolvedFixture type(ItemType type) {
        this.type = type;
        return this;
    }

    public EssaySolvedFixture sequence(int sequence) {
        this.sequence = sequence;
        return this;
    }

    public EssaySolvedFixture questionId(Long questionId) {
        this.questionId = questionId;
        return this;
    }

    public EssaySolvedFixture questionText(String questionText) {
        this.questionText = questionText;
        return this;
    }

    public EssaySolvedFixture userAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
        return this;
    }

    public EssaySolvedFixture feedback(String feedback) {
        this.feedback = feedback;
        return this;
    }

    public EssaySolvedFixture modelAnswer(String modelAnswer) {
        this.modelAnswer = modelAnswer;
        return this;
    }

    public EssaySolvedFixture isCorrect(boolean isCorrect) {
        this.isCorrect = isCorrect;
        return this;
    }

    public EssaySolvedFixture elapsedSeconds(Integer elapsedSeconds) {
        this.elapsedSeconds = elapsedSeconds;
        return this;
    }

    public EssaySolvedFixture solvedAt(LocalDateTime solvedAt) {
        this.solvedAt = solvedAt;
        return this;
    }

    public EssaySolved build() {
        return EssaySolved.create(
                solvedSessionId,
                userId,
                type,
                sequence,
                questionId,
                questionText,
                userAnswer,
                feedback,
                modelAnswer,
                isCorrect,
                elapsedSeconds,
                solvedAt
        );
    }
}
