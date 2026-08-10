package com.neogul.whynago.interview.infra;

import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.domain.InterviewStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyInterviewRepository extends JpaRepository<DailyInterview, Long> {

    Optional<DailyInterview> findByUserIdAndInterviewDate(Long userId, LocalDate interviewDate);

    boolean existsByUserIdAndInterviewDate(Long userId, LocalDate interviewDate);

    List<DailyInterview> findByUserIdAndStatusOrderByInterviewDateDesc(Long userId, InterviewStatus status);
}
