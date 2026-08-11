package com.neogul.whynago.notification.service;

import com.neogul.whynago.support.IntegrationTestSupport;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Disabled("실제 메일 발송을 눈으로 확인할 때만 TEST_EMAIL을 채우고 수동으로 실행한다")
class StudyReminderManualSendTest extends IntegrationTestSupport {

    private static final String TEST_EMAIL = "여기에 실제 수신할 이메일을 입력하세요";

    @Autowired
    private StudyReminderService studyReminderService;

    @DisplayName("학습 리마인드 메일을 실제로 발송해본다.")
    @Test
    void sendTestReminder() {
        studyReminderService.sendTestReminder(TEST_EMAIL);
    }
}
