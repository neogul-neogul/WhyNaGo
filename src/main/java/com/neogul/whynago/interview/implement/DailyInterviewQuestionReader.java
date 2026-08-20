package com.neogul.whynago.interview.implement;

import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.interview.infra.DailyInterviewQuestionRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// DailyQuestionResolver와 달리 없으면 고정하지 않는 순수 조회다(관리자 대시보드의 고정 여부 확인용).
@Component
@RequiredArgsConstructor
public class DailyInterviewQuestionReader {

    private final DailyInterviewQuestionRepository dailyInterviewQuestionRepository;

    public Optional<DailyInterviewQuestion> findByDate(LocalDate date) {
        return dailyInterviewQuestionRepository.findById(date);
    }
}
