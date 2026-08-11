package com.neogul.whynago.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.infra.NotificationSettingRepository;
import com.neogul.whynago.notification.service.dto.NotificationSettingResult;
import com.neogul.whynago.notification.service.dto.UpdateNotificationSettingCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationSettingServiceTest extends IntegrationTestSupport {

    @Autowired
    private NotificationSettingService notificationSettingService;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @DisplayName("설정이 없는 사용자가 조회하면 기본값으로 생성해 반환한다.")
    @Test
    void getSettings_createsDefaultWhenAbsent() {
        NotificationSettingResult result = notificationSettingService.getSettings(1L);

        assertThat(result.everyDayRemind()).isTrue();
        assertThat(notificationSettingRepository.findByUserId(1L)).isPresent();
    }

    @DisplayName("이미 설정이 있는 사용자를 조회하면 저장된 값을 그대로 반환한다.")
    @Test
    void getSettings_returnsExisting() {
        NotificationSetting setting = notificationSettingRepository.save(NotificationSetting.createDefault(1L));
        setting.update(false);

        NotificationSettingResult result = notificationSettingService.getSettings(1L);

        assertThat(result.everyDayRemind()).isFalse();
    }

    @DisplayName("설정을 수정하면 변경된 값이 반영된다.")
    @Test
    void updateSettings() {
        notificationSettingRepository.save(NotificationSetting.createDefault(1L));
        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(false);

        NotificationSettingResult result = notificationSettingService.updateSettings(1L, command);

        assertThat(result.everyDayRemind()).isFalse();
    }

    @DisplayName("설정이 없는 사용자를 수정하면 기본값을 생성한 뒤 수정 값을 반영한다.")
    @Test
    void updateSettings_createsDefaultWhenAbsent() {
        UpdateNotificationSettingCommand command = new UpdateNotificationSettingCommand(false);

        NotificationSettingResult result = notificationSettingService.updateSettings(1L, command);

        assertThat(result.everyDayRemind()).isFalse();
        assertThat(notificationSettingRepository.findByUserId(1L)).isPresent();
    }
}
