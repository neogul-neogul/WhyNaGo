package com.neogul.whynago.learningrecord.presentation.dto;

import com.neogul.whynago.learningrecord.service.dto.DailyRecordCountResult;
import java.time.LocalDate;

public record DailyRecordCountResponse(LocalDate date, int sessionCount, int questionCount) {

    public static DailyRecordCountResponse from(DailyRecordCountResult result) {
        return new DailyRecordCountResponse(result.date(), result.sessionCount(), result.questionCount());
    }
}