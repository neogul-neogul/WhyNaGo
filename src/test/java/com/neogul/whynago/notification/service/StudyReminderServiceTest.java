package com.neogul.whynago.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.common.mail.EmailSender;
import com.neogul.whynago.common.mail.MailErrorCode;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class StudyReminderServiceTest extends IntegrationTestSupport {

    @Autowired
    private StudyReminderService studyReminderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

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
