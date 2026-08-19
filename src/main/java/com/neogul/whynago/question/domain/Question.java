package com.neogul.whynago.question.domain;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    // 기존 행과 시드 INSERT가 컬럼을 명시하지 않으므로 DB 기본값을 걸어 SEEDED로 채운다.
    // nullable로 두면 `source <> 'GENERATED'` 같은 조건이 NULL 때문에 기존 문항을 전부 걸러낸다.
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'SEEDED'")
    @Column(nullable = false, length = 20)
    private QuestionSource source;

    // 노출 게이트다. source와 축을 나눈 이유는 QuestionReviewStatus 주석 참고.
    // source와 같은 이유로 DB 기본값을 걸어 기존 행을 APPROVED로 채운다.
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'APPROVED'")
    @Column(nullable = false, length = 20)
    private QuestionReviewStatus reviewStatus;

    // 서술형 모범답안. 시드 문항은 해설(explanation)만 갖고 채점 때 AI가 모범답안을 만들지만,
    // 생성 문항은 만들어질 때 모범답안이 함께 나오므로 문항에 붙여 보관한다.
    @Column(columnDefinition = "TEXT")
    private String modelAnswer;

    // 채점 시 반드시 언급돼야 하는 핵심 논점 목록. 생성 시점에 고정해 두지 않으면
    // 채점 기준이 호출마다 흔들린다.
    @Convert(converter = GradingCriteriaConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> gradingCriteria;

    private Question(
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation,
            QuestionSource source,
            QuestionReviewStatus reviewStatus,
            String modelAnswer,
            List<String> gradingCriteria
    ) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.explanation = explanation;
        this.source = source;
        this.reviewStatus = reviewStatus;
        this.modelAnswer = modelAnswer;
        this.gradingCriteria = gradingCriteria;
    }

    public static Question create(
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation
    ) {
        return new Question(
                title,
                content,
                type,
                difficulty,
                category,
                explanation,
                QuestionSource.SEEDED,
                QuestionReviewStatus.APPROVED,
                null,
                List.of()
        );
    }

    // 생성 문항은 항상 서술형이고, 해설 대신 모범답안·채점 기준을 가지며, 검수 전이라 PENDING으로 시작한다.
    // create(...)를 넓히는 대신 별도 팩토리를 두어 이 불변식을 구조적으로 강제한다.
    public static Question generated(
            String title,
            String content,
            Difficulty difficulty,
            Category category,
            String modelAnswer,
            List<String> gradingCriteria
    ) {
        return new Question(
                title,
                content,
                QuestionType.ESSAY,
                difficulty,
                category,
                null,
                QuestionSource.GENERATED,
                QuestionReviewStatus.PENDING,
                modelAnswer,
                List.copyOf(gradingCriteria)
        );
    }

    public void approve() {
        decide(QuestionReviewStatus.APPROVED);
    }

    public void reject() {
        decide(QuestionReviewStatus.REJECTED);
    }

    // 검수는 PENDING에서 한 번만 전이한다. 승인·거절을 되돌리는 경로는 두지 않는다.
    // 승인 후 문제가 발견되면 상태를 뒤집는 대신 별도 회수 절차로 처리한다.
    private void decide(QuestionReviewStatus decided) {
        if (reviewStatus != QuestionReviewStatus.PENDING) {
            throw new BusinessException(QuestionErrorCode.QUESTION_REVIEW_ALREADY_DECIDED);
        }
        this.reviewStatus = decided;
    }

    public boolean isEssay() {
        return type == QuestionType.ESSAY;
    }

    public boolean isMultipleChoice() {
        return type == QuestionType.MULTIPLE_CHOICE;
    }

    public boolean isGenerated() {
        return source == QuestionSource.GENERATED;
    }

    public boolean isApproved() {
        return reviewStatus == QuestionReviewStatus.APPROVED;
    }

    public boolean isRejected() {
        return reviewStatus == QuestionReviewStatus.REJECTED;
    }
}
