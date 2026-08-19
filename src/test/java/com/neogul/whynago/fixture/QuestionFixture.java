package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;

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

    // 검수 전(PENDING) 생성 문항이다.
    public static Question generatedEssay() {
        return Question.generated(
                "인덱스와 카디널리티",
                "카디널리티가 낮은 컬럼에 인덱스를 걸면 어떤 일이 벌어지는지 설명하라.",
                Difficulty.MEDIUM,
                Category.DB,
                "카디널리티가 낮으면 인덱스를 타도 걸러지는 행이 적어 풀스캔보다 느려질 수 있다.",
                List.of("카디널리티 정의", "옵티마이저가 인덱스를 버리는 이유")
        );
    }

    public static Question approvedGeneratedEssay() {
        Question question = generatedEssay();
        question.approve();
        return question;
    }

    public static Question rejectedGeneratedEssay() {
        Question question = generatedEssay();
        question.reject();
        return question;
    }

    // 카테고리·난이도·유형이 검증 의도에 중요한 테스트가 있어 빌더를 함께 둔다.
    public static QuestionBuilder builder() {
        return new QuestionBuilder();
    }

    public static final class QuestionBuilder {

        private String title = "문제 제목";
        private String content = "문제 발문이다. 무엇이 옳은지 설명하라.";
        private QuestionType type = QuestionType.MULTIPLE_CHOICE;
        private Difficulty difficulty = Difficulty.MEDIUM;
        private Category category = Category.DB;
        private String explanation = "해설이다.";

        private QuestionBuilder() {
        }

        public QuestionBuilder title(String title) {
            this.title = title;
            return this;
        }

        public QuestionBuilder content(String content) {
            this.content = content;
            return this;
        }

        public QuestionBuilder type(QuestionType type) {
            this.type = type;
            return this;
        }

        public QuestionBuilder difficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public QuestionBuilder category(Category category) {
            this.category = category;
            return this;
        }

        public Question build() {
            return Question.create(title, content, type, difficulty, category, explanation);
        }
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
