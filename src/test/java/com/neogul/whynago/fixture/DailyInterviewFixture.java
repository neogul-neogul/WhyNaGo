package com.neogul.whynago.fixture;

import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;

public final class DailyInterviewFixture {

    private DailyInterviewFixture() {
    }

    public static DailyInterview inProgress(Long userId) {
        return inProgress(userId, LocalDate.of(2026, 8, 7));
    }

    public static DailyInterview inProgress(Long userId, LocalDate interviewDate) {
        return DailyInterview.start(
                userId,
                interviewDate,
                Category.DB,
                1L,
                "conversation-id",
                LocalDateTime.of(2026, 8, 7, 9, 20, 0)
        );
    }

    public static DailyInterview completed(Long userId) {
        DailyInterview interview = inProgress(userId);
        interview.complete(100L, 2, LocalDateTime.of(2026, 8, 7, 9, 31, 40));
        return interview;
    }
}
