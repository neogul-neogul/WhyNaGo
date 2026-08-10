package com.neogul.whynago.notification.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationSettingTest {

    @DisplayName("기본값으로 알림 설정을 생성한다.")
    @Test
    void createDefault() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        assertThat(setting.getUserId()).isEqualTo(1L);
        assertThat(setting.isEveryDayRemind()).isTrue();
        assertThat(setting.getRemindTime()).isEqualTo(LocalTime.of(21, 0));
        assertThat(setting.isStreakStopPrevention()).isTrue();
        assertThat(setting.isInterviewRemind()).isFalse();
        assertThat(setting.isWeeklyReport()).isTrue();
    }

    @DisplayName("알림 설정을 수정하면 모든 필드가 새 값으로 바뀐다.")
    @Test
    void update() {
        NotificationSetting setting = NotificationSetting.createDefault(1L);

        setting.update(false, LocalTime.of(8, 0), false, true, false);

        assertThat(setting.isEveryDayRemind()).isFalse();
        assertThat(setting.getRemindTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(setting.isStreakStopPrevention()).isFalse();
        assertThat(setting.isInterviewRemind()).isTrue();
        assertThat(setting.isWeeklyReport()).isFalse();
    }
}
