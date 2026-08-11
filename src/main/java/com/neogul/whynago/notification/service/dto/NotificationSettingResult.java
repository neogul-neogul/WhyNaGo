package com.neogul.whynago.notification.service.dto;

import com.neogul.whynago.notification.domain.NotificationSetting;

public record NotificationSettingResult(
        boolean everyDayRemind
) {

    public static NotificationSettingResult from(NotificationSetting setting) {
        return new NotificationSettingResult(setting.isEveryDayRemind());
    }
}
