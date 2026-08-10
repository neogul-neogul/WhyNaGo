package com.neogul.whynago.notification.fixture;

import com.neogul.whynago.notification.domain.NotificationSetting;

public class NotificationSettingFixture {

    public static NotificationSettingBuilder notificationSetting() {
        return new NotificationSettingBuilder();
    }

    public static class NotificationSettingBuilder {

        private Long userId = 1L;

        public NotificationSettingBuilder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public NotificationSetting build() {
            return NotificationSetting.createDefault(userId);
        }
    }
}
