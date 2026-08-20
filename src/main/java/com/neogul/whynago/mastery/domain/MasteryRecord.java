package com.neogul.whynago.mastery.domain;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.question.domain.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 판정 1건의 이력이다. 한 번의 채점이 문항의 태그 개수만큼 행을 만든다.
// 현재값만 두면 "언제 어떤 근거로 그 판정을 받았는지"를 되짚을 수 없어 이력을 따로 남긴다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasteryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long questionId;

    // 태그가 없는 문항은 null이다. 이 경우 카테고리 단위 신호로만 남는다.
    private Long tagId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MasteryLevel level;

    // 판정 근거. AI 판정은 답변에서 근거를 짚은 문장이고, 규칙 판정은 서버가 만든 요약이다.
    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MasterySource source;

    // 서술형 대화의 몇 번째 턴인지다(1 = 본질문). 객관식은 턴 개념이 없어 null이고,
    // 턴을 싣기 전에 쌓인 이력도 null이다. null은 "본질문"이 아니라 "미지"로 취급한다.
    private Integer turn;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private MasteryRecord(
            Long userId,
            Long questionId,
            Long tagId,
            Category category,
            MasteryLevel level,
            String reason,
            MasterySource source,
            Integer turn,
            LocalDateTime createdAt
    ) {
        this.userId = userId;
        this.questionId = questionId;
        this.tagId = tagId;
        this.category = category;
        this.level = level;
        this.reason = reason;
        this.source = source;
        this.turn = turn;
        this.createdAt = createdAt;
    }

    public static MasteryRecord of(
            Long userId,
            Long questionId,
            Long tagId,
            Category category,
            MasteryLevel level,
            String reason,
            MasterySource source,
            Integer turn,
            LocalDateTime createdAt
    ) {
        return new MasteryRecord(userId, questionId, tagId, category, level, reason, source, turn, createdAt);
    }
}
