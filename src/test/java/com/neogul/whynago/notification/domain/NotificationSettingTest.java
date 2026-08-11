package com.neogul.whynago.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationSettingTest {

    @DisplayName("기본값으로 알림 설정을 생성한다.")
    @Test
    void createDefault() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        assertThat(setting.getUserId()).isEqualTo(1L);
        assertThat(setting.isEveryDayRemind()).isTrue();
    }

    @DisplayName("알림 설정을 수정하면 필드가 새 값으로 바뀐다.")
    @Test
    void update() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        setting.update(false);

        assertThat(setting.isEveryDayRemind()).isFalse();
    }
}
