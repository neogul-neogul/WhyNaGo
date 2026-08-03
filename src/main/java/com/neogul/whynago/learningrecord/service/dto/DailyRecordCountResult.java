package com.neogul.whynago.learningrecord.service.dto;

import com.neogul.whynago.learningrecord.implement.dto.DailyCount;
import java.time.LocalDate;

public record DailyRecordCountResult(LocalDate date, int sessionCount, int questionCount) {

    public static DailyRecordCountResult from(DailyCount dailyCount) {
        return new DailyRecordCountResult(dailyCount.date(), dailyCount.sessionCount(), dailyCount.questionCount());
    }
}