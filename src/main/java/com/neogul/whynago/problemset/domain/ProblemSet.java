package com.neogul.whynago.problemset.domain;

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
public class ProblemSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private ProblemSet(Long userId, String name) {
        this.userId = userId;
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public static ProblemSet create(Long userId, String name) {
        return new ProblemSet(userId, name);
    }

    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
