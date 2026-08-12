package com.neogul.whynago.problemset.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_problem_set_item_set_question", columnNames = {"problem_set_id", "question_id"}))
public class ProblemSetItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long problemSetId;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private LocalDateTime addedAt;

    private ProblemSetItem(Long problemSetId, Long questionId) {
        this.problemSetId = problemSetId;
        this.questionId = questionId;
        this.addedAt = LocalDateTime.now();
    }

    public static ProblemSetItem create(Long problemSetId, Long questionId) {
        return new ProblemSetItem(problemSetId, questionId);
    }
}
