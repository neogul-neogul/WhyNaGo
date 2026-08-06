package com.neogul.whynago.notification.service;

import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.implement.NotificationSettingAppender;
import com.neogul.whynago.notification.implement.NotificationSettingReader;
import com.neogul.whynago.notification.service.dto.NotificationSettingResult;
import com.neogul.whynago.notification.service.dto.UpdateNotificationSettingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingReader notificationSettingReader;
    private final NotificationSettingAppender notificationSettingAppender;

    @Transactional
    public NotificationSettingResult getSettings(Long userId) {
        NotificationSetting setting = findOrCreateDefault(userId);
        return NotificationSettingResult.from(setting);
    }

    @Transactional
    public NotificationSettingResult updateSettings(Long userId, UpdateNotificationSettingCommand command) {
        NotificationSetting setting = findOrCreateDefault(userId);
        setting.update(
                command.everyDayRemind(),
                command.remindTime(),
                command.streakStopPrevention(),
                command.wrongNote(),
                command.interviewRemind(),
                command.weeklyReport());
        return NotificationSettingResult.from(setting);
    }

    private NotificationSetting findOrCreateDefault(Long userId) {
        return notificationSettingReader.findByUserId(userId)
                .orElseGet(() -> notificationSettingAppender.appendDefault(userId));
    }
}
