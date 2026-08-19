package com.neogul.whynago.interview.implement;

import com.neogul.whynago.interview.domain.InterviewStatus;
import com.neogul.whynago.interview.implement.dto.DailyInterviewCount;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// DailyInterviewReader는 사용자 단위 조회를 담당하고, 이 클래스는 일자별 전체 집계를 담당한다.
@Component
@RequiredArgsConstructor
public class InterviewStatisticsReader {

    private final DailyInterviewRepository dailyInterviewRepository;

    public DailyInterviewCount countByDate(LocalDate date) {
        long startedCount = dailyInterviewRepository.countByInterviewDate(date);
        long completedCount = dailyInterviewRepository.countByInterviewDateAndStatus(date, InterviewStatus.COMPLETED);
        return new DailyInterviewCount(startedCount, completedCount);
    }
}
