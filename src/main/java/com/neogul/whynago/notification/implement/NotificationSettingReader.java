package com.neogul.whynago.notification.implement;

import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.infra.NotificationSettingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSettingReader {

    private final NotificationSettingRepository notificationSettingRepository;

    public Optional<NotificationSetting> findByUserId(Long userId) {
        return notificationSettingRepository.findByUserId(userId);
    }
}
