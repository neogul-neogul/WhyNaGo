package com.neogul.whynago.admin.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.domain.DashboardAlertType;
import com.neogul.whynago.admin.implement.dto.DashboardAlert;
import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.interview.infra.DailyInterviewQuestionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DashboardAlertDetectorTest extends IntegrationTestSupport {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 19);

    @Autowired
    private DashboardAlertDetector dashboardAlertDetector;

    @Autowired
    private DailyInterviewQuestionRepository dailyInterviewQuestionRepository;

    @Test
    @DisplayName("오늘 면접 문항이 고정되지 않았으면 미고정 알림을 만든다.")
    void detect_notPinned() {
        List<DashboardAlert> alerts = dashboardAlertDetector.detect(TODAY);

        assertThat(alerts).extracting(DashboardAlert::type, DashboardAlert::interviewDate)
                .containsExactly(tuple(DashboardAlertType.DAILY_INTERVIEW_NOT_PINNED, TODAY));
    }

    @Test
    @DisplayName("오늘 면접 문항이 고정돼 있으면 알림이 없다.")
    void detect_pinned() {
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(TODAY, 1L));

        List<DashboardAlert> alerts = dashboardAlertDetector.detect(TODAY);

        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("다른 날짜의 고정 이력은 오늘 미고정 판정에 영향을 주지 않는다.")
    void detect_pinnedOnOtherDate() {
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(TODAY.minusDays(1), 1L));

        List<DashboardAlert> alerts = dashboardAlertDetector.detect(TODAY);

        assertThat(alerts).extracting(DashboardAlert::type)
                .containsExactly(DashboardAlertType.DAILY_INTERVIEW_NOT_PINNED);
    }
}
