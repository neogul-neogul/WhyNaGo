package com.neogul.whynago.notification.scheduler;

import com.neogul.whynago.notification.service.StudyReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyReminderScheduler {

    private final StudyReminderService studyReminderService;

    @Scheduled(cron = "0 0 21 * * *", zone = "Asia/Seoul")
    public void sendDailyReminders() {
        studyReminderService.sendDailyReminders();
    }
}
