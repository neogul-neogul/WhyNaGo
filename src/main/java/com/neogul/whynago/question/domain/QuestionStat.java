package com.neogul.whynago.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 사용자 무관 전역 통계다. 야간 배치가 풀이 이력을 집계해 갱신하며,
// 추천의 숙련도 판정(내 소요시간 / 문제 평균 소요시간)과 문제은행 정답률 표시가 함께 쓴다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuestionStat {

    // question 1건당 통계 1건이므로 questionId를 그대로 PK로 쓴다.
    @Id
    private Long questionId;

    // 소요 시간을 보고한 표본이 하나도 없으면 null이며 "평균 없음"을 뜻한다(0초가 아니다).
    private Integer avgElapsedSeconds;

    @Column(nullable = false)
    private double correctRate;

    @Column(nullable = false)
    private int sampleCount;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private QuestionStat(
            Long questionId,
            Integer avgElapsedSeconds,
            double correctRate,
            int sampleCount,
            LocalDateTime updatedAt
    ) {
        this.questionId = questionId;
        this.avgElapsedSeconds = avgElapsedSeconds;
        this.correctRate = correctRate;
        this.sampleCount = sampleCount;
        this.updatedAt = updatedAt;
    }

    public static QuestionStat of(
            Long questionId,
            Integer avgElapsedSeconds,
            double correctRate,
            int sampleCount,
            LocalDateTime updatedAt
    ) {
        return new QuestionStat(questionId, avgElapsedSeconds, correctRate, sampleCount, updatedAt);
    }

    // 배치는 매번 전량을 다시 계산하므로 누적이 아니라 덮어쓴다.
    public void refresh(Integer avgElapsedSeconds, double correctRate, int sampleCount, LocalDateTime updatedAt) {
        this.avgElapsedSeconds = avgElapsedSeconds;
        this.correctRate = correctRate;
        this.sampleCount = sampleCount;
        this.updatedAt = updatedAt;
    }
}
