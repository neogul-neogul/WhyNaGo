package com.neogul.whynago.common.mail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;

import com.neogul.whynago.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class EmailSenderTest {

    private final JavaMailSender javaMailSender = mock(JavaMailSender.class);
    private final EmailSender emailSender = new EmailSender(javaMailSender, "from@example.com");

    @DisplayName("이메일을 발송한다.")
    @Test
    void send() {
        emailSender.send("to@example.com", "제목", "본문");

        then(javaMailSender).should().send(any(SimpleMailMessage.class));
    }

    @DisplayName("발송에 실패하면 프로젝트 예외로 변환한다.")
    @Test
    void send_mailServerFailure() {
        willThrow(new MailSendException("smtp down")).given(javaMailSender).send(any(SimpleMailMessage.class));

        assertThatThrownBy(() -> emailSender.send("to@example.com", "제목", "본문"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(MailErrorCode.MAIL_SEND_FAILED));
    }
}
