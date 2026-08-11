package com.neogul.whynago.notification.presentation.dto;

import com.neogul.whynago.notification.service.dto.UpdateNotificationSettingCommand;

public record UpdateNotificationSettingRequest(
        boolean everyDayRemind
) {

    public UpdateNotificationSettingCommand toCommand() {
        return new UpdateNotificationSettingCommand(everyDayRemind);
    }
}
