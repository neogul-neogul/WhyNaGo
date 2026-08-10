package com.neogul.whynago.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    private static final LocalTime DEFAULT_REMIND_TIME = LocalTime.of(21, 0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private boolean everyDayRemind;

    @Column(nullable = false)
    private LocalTime remindTime;

    @Column(nullable = false)
    private boolean streakStopPrevention;

    @Column(nullable = false)
    private boolean interviewRemind;

    @Column(nullable = false)
    private boolean weeklyReport;

    private NotificationSetting(
            Long userId,
            boolean everyDayRemind,
            LocalTime remindTime,
            boolean streakStopPrevention,
            boolean interviewRemind,
            boolean weeklyReport) {
        this.userId = userId;
        this.everyDayRemind = everyDayRemind;
        this.remindTime = remindTime;
        this.streakStopPrevention = streakStopPrevention;
        this.interviewRemind = interviewRemind;
        this.weeklyReport = weeklyReport;
    }

    public static NotificationSetting createDefault(Long userId) {
        return new NotificationSetting(userId, true, DEFAULT_REMIND_TIME, true, false, true);
    }

    public void update(
            boolean everyDayRemind,
            LocalTime remindTime,
            boolean streakStopPrevention,
            boolean interviewRemind,
            boolean weeklyReport) {
        this.everyDayRemind = everyDayRemind;
        this.remindTime = remindTime;
        this.streakStopPrevention = streakStopPrevention;
        this.interviewRemind = interviewRemind;
        this.weeklyReport = weeklyReport;
    }
}
