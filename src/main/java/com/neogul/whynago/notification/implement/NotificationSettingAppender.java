package com.neogul.whynago.notification.implement;

import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.infra.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSettingAppender {

    private final NotificationSettingRepository notificationSettingRepository;

    public NotificationSetting appendDefault(Long userId) {
        return notificationSettingRepository.save(NotificationSetting.createDefault(userId));
    }

    public void appendDefaultIfAbsent(Long userId) {
        if (notificationSettingRepository.existsByUserId(userId)) {
            return;
        }
        notificationSettingRepository.save(NotificationSetting.createDefault(userId));
    }
}
