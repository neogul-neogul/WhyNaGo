package com.neogul.whynago.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private boolean everyDayRemind;

    private NotificationSetting(Long userId, boolean everyDayRemind) {
        this.userId = userId;
        this.everyDayRemind = everyDayRemind;
    }

    public static NotificationSetting createDefault(Long userId) {
        return new NotificationSetting(userId, true);
    }

    public void update(boolean everyDayRemind) {
        this.everyDayRemind = everyDayRemind;
    }
}
