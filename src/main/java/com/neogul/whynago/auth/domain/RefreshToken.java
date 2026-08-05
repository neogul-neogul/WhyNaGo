package com.neogul.whynago.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String tokenHash;

    private LocalDateTime usedAt;

    private RefreshToken(Long userId, String tokenHash) {
        this.userId = userId;
        this.tokenHash = tokenHash;
    }

    public static RefreshToken create(Long userId, String tokenHash) {
        return new RefreshToken(userId, tokenHash);
    }

    // 최초 사용 시각을 고정한다. 갱신하면 유예 창이 재사용마다 밀려 닫히지 않는다.
    public void markUsed(LocalDateTime usedAt) {
        if (this.usedAt == null) {
            this.usedAt = usedAt;
        }
    }

    public boolean isUsedBefore(LocalDateTime graceStart) {
        return usedAt != null && !usedAt.isAfter(graceStart);
    }
}