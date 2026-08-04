package com.neogul.whynago.learningrecord.implement.dto;

import java.time.LocalDate;

public record DailyCount(LocalDate date, int sessionCount, int questionCount) {
}