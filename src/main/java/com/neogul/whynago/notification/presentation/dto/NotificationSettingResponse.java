package com.neogul.whynago.notification.presentation.dto;

import com.neogul.whynago.notification.service.dto.NotificationSettingResult;
import java.time.LocalTime;

public record NotificationSettingResponse(
        boolean everyDayRemind,
        LocalTime remindTime,
        boolean streakStopPrevention,
        boolean interviewRemind,
        boolean weeklyReport
) {

    public static NotificationSettingResponse from(NotificationSettingResult result) {
        return new NotificationSettingResponse(
                result.everyDayRemind(),
                result.remindTime(),
                result.streakStopPrevention(),
                result.interviewRemind(),
                result.weeklyReport());
    }
}
