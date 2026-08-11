package com.neogul.whynago.notification.presentation.dto;

import com.neogul.whynago.notification.service.dto.NotificationSettingResult;

public record NotificationSettingResponse(
        boolean everyDayRemind
) {

    public static NotificationSettingResponse from(NotificationSettingResult result) {
        return new NotificationSettingResponse(result.everyDayRemind());
    }
}
