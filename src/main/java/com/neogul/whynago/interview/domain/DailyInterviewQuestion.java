package com.neogul.whynago.interview.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyInterviewQuestion {

    @Id
    private LocalDate interviewDate;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false)
    private LocalDateTime pinnedAt;

    private DailyInterviewQuestion(LocalDate interviewDate, Long questionId, LocalDateTime pinnedAt) {
        this.interviewDate = interviewDate;
        this.questionId = questionId;
        this.pinnedAt = pinnedAt;
    }

    public static DailyInterviewQuestion pin(LocalDate interviewDate, Long questionId) {
        return new DailyInterviewQuestion(interviewDate, questionId, LocalDateTime.now());
    }
}
