package com.neogul.whynago.notification.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.notification.domain.NotificationSetting;
import com.neogul.whynago.notification.fixture.NotificationSettingFixture;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationSettingRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private NotificationSettingRepository notificationSettingRepository;

    @DisplayName("userId로 알림 설정을 조회한다.")
    @Test
    void findByUserId() {
        NotificationSetting setting = NotificationSettingFixture.notificationSetting().userId(1L).build();
        em.persistAndFlush(setting);
        em.clear();

        Optional<NotificationSetting> found = notificationSettingRepository.findByUserId(1L);

        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(1L);
    }

    @DisplayName("해당 userId의 알림 설정이 없으면 빈 값을 반환한다.")
    @Test
    void findByUserId_notFound() {
        Optional<NotificationSetting> found = notificationSettingRepository.findByUserId(999L);

        assertThat(found).isEmpty();
    }

    @DisplayName("everyDayRemind가 true인 설정만 조회한다.")
    @Test
    void findAllByEveryDayRemindTrue() {
        NotificationSetting enabled = NotificationSettingFixture.notificationSetting().userId(1L).build();
        NotificationSetting disabled = NotificationSettingFixture.notificationSetting().userId(2L).build();
        disabled.update(false);
        em.persistAndFlush(enabled);
        em.persistAndFlush(disabled);
        em.clear();

        List<NotificationSetting> found = notificationSettingRepository.findAllByEveryDayRemindTrue();

        assertThat(found).extracting(NotificationSetting::getUserId).containsExactly(1L);
    }
}
