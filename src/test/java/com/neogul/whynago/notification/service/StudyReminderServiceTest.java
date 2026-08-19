package com.neogul.whynago.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.mail.EmailSender;
import com.neogul.whynago.common.mail.MailErrorCode;
import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.infra.EmailBatchExecutionRepository;
import com.neogul.whynago.emailbatch.infra.EmailSendLogRepository;
import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.infra.NotificationSettingRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSendException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class StudyReminderServiceTest extends IntegrationTestSupport {

    @Autowired
    private StudyReminderService studyReminderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @Autowired
    private EmailBatchExecutionRepository emailBatchExecutionRepository;

    @Autowired
    private EmailSendLogRepository emailSendLogRepository;

    @MockitoBean
    private EmailSender emailSender;

    @DisplayName("매일 리마인드가 켜진 사용자에게만 메일을 보낸다.")
    @Test
    void sendDailyReminders() {
        User enabledUser = userRepository.save(
                UserFixture.user().email("enabled@example.com").nickname("enabled").build());
        User disabledUser = userRepository.save(
                UserFixture.user().email("disabled@example.com").nickname("disabled").build());
        notificationSettingRepository.save(NotificationSetting.createDefault(enabledUser.getId()));
        NotificationSetting disabledSetting = NotificationSetting.createDefault(disabledUser.getId());
        disabledSetting.update(false);
        notificationSettingRepository.save(disabledSetting);

        studyReminderService.sendDailyReminders();

        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        then(emailSender).should().sendHtml(toCaptor.capture(), anyString(), anyString());
        assertThat(toCaptor.getValue()).isEqualTo("enabled@example.com");
    }

    @DisplayName("일부 사용자의 발송이 실패해도 나머지 사용자에게 발송한다.")
    @Test
    void sendDailyReminders_partialFailure() {
        User failUser = userRepository.save(
                UserFixture.user().email("fail@example.com").nickname("fail").build());
        User successUser = userRepository.save(
                UserFixture.user().email("success@example.com").nickname("success").build());
        notificationSettingRepository.save(NotificationSetting.createDefault(failUser.getId()));
        notificationSettingRepository.save(NotificationSetting.createDefault(successUser.getId()));
        willThrow(new BusinessException(MailErrorCode.MAIL_SEND_FAILED))
                .given(emailSender).sendHtml(eq("fail@example.com"), anyString(), anyString());

        assertThatCode(() -> studyReminderService.sendDailyReminders())
                .doesNotThrowAnyException();

        then(emailSender).should().sendHtml(eq("success@example.com"), anyString(), anyString());
    }

    @DisplayName("배치 실행 이력과 수신자별 발송 기록을 남긴다.")
    @Test
    void sendDailyReminders_recordsHistory() {
        User user = userRepository.save(UserFixture.user().email("enabled@example.com").build());
        notificationSettingRepository.save(NotificationSetting.createDefault(user.getId()));

        studyReminderService.sendDailyReminders();

        assertThat(emailBatchExecutionRepository.findAll())
                .hasSize(1)
                .first()
                .extracting(
                        EmailBatchExecution::getTotalTargetCount,
                        EmailBatchExecution::getSuccessCount,
                        EmailBatchExecution::getFailureCount,
                        EmailBatchExecution::getStatus)
                .containsExactly(1, 1, 0, EmailBatchStatus.SUCCESS);
        assertThat(emailSendLogRepository.findAll())
                .hasSize(1)
                .first()
                .extracting(EmailSendLog::getUserId, EmailSendLog::getRecipientEmail, EmailSendLog::getStatus)
                .containsExactly(user.getId(), "enabled@example.com", EmailSendStatus.SUCCESS);
    }

    @DisplayName("발송이 실패하면 실패 사유를 담은 기록과 실패 건수를 남긴다.")
    @Test
    void sendDailyReminders_recordsFailureReason() {
        User failUser = userRepository.save(
                UserFixture.user().email("fail@example.com").nickname("fail").build());
        User successUser = userRepository.save(
                UserFixture.user().email("success@example.com").nickname("success").build());
        notificationSettingRepository.save(NotificationSetting.createDefault(failUser.getId()));
        notificationSettingRepository.save(NotificationSetting.createDefault(successUser.getId()));
        willThrow(new BusinessException(MailErrorCode.MAIL_SEND_FAILED, new MailSendException("Mailbox full")))
                .given(emailSender).sendHtml(eq("fail@example.com"), anyString(), anyString());

        studyReminderService.sendDailyReminders();

        assertThat(emailBatchExecutionRepository.findAll())
                .hasSize(1)
                .first()
                .extracting(
                        EmailBatchExecution::getSuccessCount,
                        EmailBatchExecution::getFailureCount,
                        EmailBatchExecution::getStatus)
                .containsExactly(1, 1, EmailBatchStatus.PARTIAL_FAILURE);
        // 도메인 에러 메시지가 아니라 실제 원인이 남아야 관리자가 실패 사유를 구분할 수 있다.
        assertThat(emailSendLogRepository.findAll())
                .filteredOn(sendLog -> sendLog.getStatus() == EmailSendStatus.FAILURE)
                .extracting(EmailSendLog::getRecipientEmail, EmailSendLog::getFailureReason)
                .containsExactly(tuple("fail@example.com", "Mailbox full"));
    }

    @DisplayName("설정과 무관하게 이메일로 지정한 사용자에게 테스트 메일을 보낸다.")
    @Test
    void sendTestReminder() {
        userRepository.save(UserFixture.user().email("target@example.com").build());

        studyReminderService.sendTestReminder("target@example.com");

        then(emailSender).should().sendHtml(eq("target@example.com"), anyString(), anyString());
    }

    @DisplayName("존재하지 않는 이메일로 테스트 메일을 요청하면 예외가 발생한다.")
    @Test
    void sendTestReminder_userNotFound() {
        assertThatThrownBy(() -> studyReminderService.sendTestReminder("nobody@example.com"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
        then(emailSender).shouldHaveNoInteractions();
    }
}
