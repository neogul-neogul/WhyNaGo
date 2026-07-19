package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.AnswerChoice;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerChoiceRepository extends JpaRepository<AnswerChoice, Long> {

    List<AnswerChoice> findByQuestionIdOrderBySequence(Long questionId);

    Optional<AnswerChoice> findFirstByQuestionIdAndIsCorrectTrue(Long questionId);
}
