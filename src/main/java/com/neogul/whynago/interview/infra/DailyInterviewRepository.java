package com.neogul.whynago.interview.infra;

import com.neogul.whynago.interview.domain.DailyInterview;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyInterviewRepository extends JpaRepository<DailyInterview, Long> {

    Optional<DailyInterview> findByUserIdAndInterviewDate(Long userId, LocalDate interviewDate);

    boolean existsByUserIdAndInterviewDate(Long userId, LocalDate interviewDate);
}
