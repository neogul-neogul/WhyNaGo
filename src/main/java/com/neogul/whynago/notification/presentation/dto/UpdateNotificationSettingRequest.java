package com.neogul.whynago.notification.presentation.dto;

import com.neogul.whynago.notification.service.dto.UpdateNotificationSettingCommand;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record UpdateNotificationSettingRequest(
        boolean everyDayRemind,
        @NotNull LocalTime remindTime,
        boolean streakStopPrevention,
        boolean interviewRemind,
        boolean weeklyReport
) {

    public UpdateNotificationSettingCommand toCommand() {
        return new UpdateNotificationSettingCommand(
                everyDayRemind, remindTime, streakStopPrevention, interviewRemind, weeklyReport);
    }
}
