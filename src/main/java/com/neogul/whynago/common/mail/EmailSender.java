package com.neogul.whynago.common.mail;

import com.neogul.whynago.common.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * 특정 도메인에 종속되지 않는 공용 이메일 발송 컴포넌트.
 * 텍스트·HTML 메일을 지원한다 — 첨부파일·다중 수신자가 필요해지면 그때 확장한다.
 */
@Slf4j
@Component
public class EmailSender {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    public EmailSender(JavaMailSender javaMailSender, @Value("${mail.from}") String fromAddress) {
        this.javaMailSender = javaMailSender;
        this.fromAddress = fromAddress;
    }

    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("이메일 발송 실패 - to={}", to, e);
            throw new BusinessException(MailErrorCode.MAIL_SEND_FAILED, e);
        }
    }

    public void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException | MailException e) {
            log.error("이메일 발송 실패 - to={}", to, e);
            // 발송 이력의 실패 사유로 실제 원인(SMTP 응답 등)을 남길 수 있게 원인을 보존한다.
            throw new BusinessException(MailErrorCode.MAIL_SEND_FAILED, e);
        }
    }
}
