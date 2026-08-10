package com.neogul.whynago.interview.infra;

import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyInterviewQuestionRepository extends JpaRepository<DailyInterviewQuestion, LocalDate> {

    @Modifying
    @Query(value = """
            insert ignore into daily_interview_question (interview_date, question_id, pinned_at)
            values (:interviewDate, :questionId, :pinnedAt)
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("interviewDate") LocalDate interviewDate,
            @Param("questionId") Long questionId,
            @Param("pinnedAt") LocalDateTime pinnedAt
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DailyInterviewQuestion d where d.interviewDate = :interviewDate")
    Optional<DailyInterviewQuestion> findByDateWithLock(@Param("interviewDate") LocalDate interviewDate);
}
