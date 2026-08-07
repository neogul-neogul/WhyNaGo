package com.neogul.whynago.interview.infra;

import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyInterviewQuestionRepository extends JpaRepository<DailyInterviewQuestion, LocalDate> {
}
