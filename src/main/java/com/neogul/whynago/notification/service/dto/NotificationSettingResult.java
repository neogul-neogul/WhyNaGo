package com.neogul.whynago.notification.service.dto;

import com.neogul.whynago.notification.domain.NotificationSetting;
import java.time.LocalTime;

public record NotificationSettingResult(
        boolean everyDayRemind,
        LocalTime remindTime,
        boolean streakStopPrevention,
        boolean interviewRemind,
        boolean weeklyReport
) {

    public static NotificationSettingResult from(NotificationSetting setting) {
        return new NotificationSettingResult(
                setting.isEveryDayRemind(),
                setting.getRemindTime(),
                setting.isStreakStopPrevention(),
                setting.isInterviewRemind(),
                setting.isWeeklyReport());
    }
}
