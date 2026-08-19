package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.QuestionStat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionStatRepository extends JpaRepository<QuestionStat, Long> {
}
