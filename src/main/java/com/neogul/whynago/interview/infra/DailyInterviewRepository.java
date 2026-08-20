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

    long countByUserIdAndStatus(Long userId, InterviewStatus status);

    // 아래 두 집계는 특정 사용자가 아니라 해당 일자의 전체 면접을 센다(관리자 대시보드).
    long countByInterviewDate(LocalDate interviewDate);

    long countByInterviewDateAndStatus(LocalDate interviewDate, InterviewStatus status);
}
