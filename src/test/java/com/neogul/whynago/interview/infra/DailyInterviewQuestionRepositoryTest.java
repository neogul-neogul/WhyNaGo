package com.neogul.whynago.interview.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DailyInterviewQuestionRepositoryTest extends RepositoryTestSupport {

    private static final LocalDate INTERVIEW_DATE = LocalDate.of(2026, 8, 7);

    @Autowired
    private DailyInterviewQuestionRepository dailyInterviewQuestionRepository;

    @Test
    @DisplayName("날짜로 그날 고정된 질문을 조회한다.")
    void findById() {
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(INTERVIEW_DATE, 7L));

        DailyInterviewQuestion pinned = dailyInterviewQuestionRepository.findById(INTERVIEW_DATE).orElseThrow();

        assertThat(pinned.getQuestionId()).isEqualTo(7L);
        assertThat(pinned.getPinnedAt()).isNotNull();
    }

    @Test
    @DisplayName("고정되지 않은 날짜는 조회되지 않는다.")
    void findByIdWhenNotPinned() {
        assertThat(dailyInterviewQuestionRepository.findById(INTERVIEW_DATE)).isEmpty();
    }

    @Test
    @DisplayName("날짜가 기본키라 하루에 한 행만 남는다.")
    void saveKeepsSingleRowPerDate() {
        dailyInterviewQuestionRepository.saveAndFlush(DailyInterviewQuestion.pin(INTERVIEW_DATE, 7L));
        dailyInterviewQuestionRepository.saveAndFlush(DailyInterviewQuestion.pin(INTERVIEW_DATE, 8L));

        assertThat(dailyInterviewQuestionRepository.count()).isEqualTo(1);
    }
}
