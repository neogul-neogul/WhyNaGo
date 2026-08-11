package com.neogul.whynago.notification.service;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.notification.implement.NotificationSettingReader;
import com.neogul.whynago.notification.implement.StudyReminderMailSender;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.implement.UserReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyReminderService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final NotificationSettingReader notificationSettingReader;
    private final UserReader userReader;
    private final SolvedSessionReader solvedSessionReader;
    private final StudyReminderMailSender studyReminderMailSender;

    @Transactional(readOnly = true)
    public void sendDailyReminders() {
        notificationSettingReader.findAllEveryDayRemindEnabled()
                .forEach(setting -> sendTo(userReader.read(setting.getUserId())));
    }

    @Transactional(readOnly = true)
    public void sendTestReminder(String email) {
        User user = userReader.findByEmail(email)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
        sendTo(user);
    }

    private void sendTo(User user) {
        LocalDate today = LocalDate.now(KST);
        boolean solvedToday = !solvedSessionReader
                .readBetween(user.getId(), today.atStartOfDay(), today.atTime(LocalTime.MAX))
                .isEmpty();
        boolean interviewDoneToday = false; // 1일 1면접 도메인이 없어 고정값. 기능 추가 시 이 값만 교체한다.
        studyReminderMailSender.send(user.getEmail().getValue(), solvedToday, interviewDoneToday);
    }
}
