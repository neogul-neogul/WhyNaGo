package com.neogul.whynago.mastery.domain;

import com.neogul.whynago.common.domain.MasteryLevel;
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

// 사용자 x 태그의 현재 숙련도다. 새 판정이 오면 누적하지 않고 덮어쓴다.
// 숙련도는 "지금 이 주제를 얼마나 아는가"이므로 과거 판정을 평균하면 최근 학습이 묻힌다.
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserTagMastery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long tagId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MasteryLevel level;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private UserTagMastery(Long userId, Long tagId, MasteryLevel level, String reason, LocalDateTime updatedAt) {
        this.userId = userId;
        this.tagId = tagId;
        this.level = level;
        this.reason = reason;
        this.updatedAt = updatedAt;
    }

    public static UserTagMastery of(
            Long userId,
            Long tagId,
            MasteryLevel level,
            String reason,
            LocalDateTime updatedAt
    ) {
        return new UserTagMastery(userId, tagId, level, reason, updatedAt);
    }

    public void refresh(MasteryLevel level, String reason, LocalDateTime updatedAt) {
        this.level = level;
        this.reason = reason;
        this.updatedAt = updatedAt;
    }
}
