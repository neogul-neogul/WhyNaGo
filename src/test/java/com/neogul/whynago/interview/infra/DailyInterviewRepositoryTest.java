package com.neogul.whynago.interview.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.fixture.DailyInterviewFixture;
import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.domain.InterviewStatus;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DailyInterviewRepositoryTest extends RepositoryTestSupport {

    private static final LocalDate INTERVIEW_DATE = LocalDate.of(2026, 8, 7);

    @Autowired
    private DailyInterviewRepository dailyInterviewRepository;

    @Test
    @DisplayName("사용자와 날짜로 그날의 면접을 조회한다.")
    void findByUserIdAndInterviewDate() {
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE));

        DailyInterview found = dailyInterviewRepository.findByUserIdAndInterviewDate(10L, INTERVIEW_DATE).orElseThrow();

        assertThat(found.getUserId()).isEqualTo(10L);
        assertThat(found.getInterviewDate()).isEqualTo(INTERVIEW_DATE);
    }

    @Test
    @DisplayName("다른 날짜의 면접은 조회되지 않는다.")
    void findByUserIdAndInterviewDateWithOtherDate() {
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE));

        assertThat(dailyInterviewRepository.findByUserIdAndInterviewDate(10L, INTERVIEW_DATE.plusDays(1))).isEmpty();
    }

    @Test
    @DisplayName("같은 사용자가 같은 날 두 번 면접을 저장하면 제약 위반이 발생한다.")
    void saveDuplicatedOnSameDay() {
        dailyInterviewRepository.saveAndFlush(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE));

        assertThatThrownBy(() -> dailyInterviewRepository.saveAndFlush(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE)))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("다른 사용자는 같은 날 각자 면접을 저장할 수 있다.")
    void saveDifferentUsersOnSameDay() {
        dailyInterviewRepository.saveAndFlush(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE));
        dailyInterviewRepository.saveAndFlush(DailyInterviewFixture.inProgress(11L, INTERVIEW_DATE));

        assertThat(dailyInterviewRepository.existsByUserIdAndInterviewDate(10L, INTERVIEW_DATE)).isTrue();
        assertThat(dailyInterviewRepository.existsByUserIdAndInterviewDate(11L, INTERVIEW_DATE)).isTrue();
    }

    @Test
    @DisplayName("완료된 면접만 최신 날짜순으로 조회한다.")
    void findByUserIdAndStatusOrderByInterviewDateDesc() {
        dailyInterviewRepository.save(DailyInterviewFixture.completed(10L, INTERVIEW_DATE.minusDays(1)));
        dailyInterviewRepository.save(DailyInterviewFixture.completed(10L, INTERVIEW_DATE));
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE.plusDays(1)));

        List<DailyInterview> found = dailyInterviewRepository
                .findByUserIdAndStatusOrderByInterviewDateDesc(10L, InterviewStatus.COMPLETED);

        assertThat(found).extracting(DailyInterview::getInterviewDate)
                .containsExactly(INTERVIEW_DATE, INTERVIEW_DATE.minusDays(1));
    }

    @Test
    @DisplayName("완료된 면접이 없으면 빈 목록을 반환한다.")
    void findByUserIdAndStatusOrderByInterviewDateDescWhenEmpty() {
        dailyInterviewRepository.save(DailyInterviewFixture.inProgress(10L, INTERVIEW_DATE));

        assertThat(dailyInterviewRepository
                .findByUserIdAndStatusOrderByInterviewDateDesc(10L, InterviewStatus.COMPLETED)).isEmpty();
    }
}
