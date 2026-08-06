package com.neogul.whynago.notification.service.dto;

import java.time.LocalTime;

public record UpdateNotificationSettingCommand(
        boolean everyDayRemind,
        LocalTime remindTime,
        boolean streakStopPrevention,
        boolean wrongNote,
        boolean interviewRemind,
        boolean weeklyReport
) {
}
