package com.neogul.whynago.notification.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.neogul.whynago.common.mail.EmailSender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class StudyReminderMailSenderTest {

    private final EmailSender emailSender = mock(EmailSender.class);
    private final StudyReminderMailSender studyReminderMailSender = new StudyReminderMailSender(emailSender);

    @DisplayName("완료한 항목은 본문에 완료 배지를 담아 발송한다.")
    @Test
    void send_done() {
        studyReminderMailSender.send("to@example.com", true, true);

        String body = capturedBody();
        assertThat(body).contains(">완료<");
        assertThat(body).doesNotContain(">미완료<");
    }

    @DisplayName("완료하지 않은 항목은 본문에 미완료 배지를 담아 발송한다.")
    @Test
    void send_notDone() {
        studyReminderMailSender.send("to@example.com", false, false);

        String body = capturedBody();
        assertThat(body).contains(">미완료<");
        assertThat(body).doesNotContain(">완료<");
    }

    private String capturedBody() {
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        then(emailSender).should().sendHtml(eq("to@example.com"), any(), bodyCaptor.capture());
        return bodyCaptor.getValue();
    }
}
