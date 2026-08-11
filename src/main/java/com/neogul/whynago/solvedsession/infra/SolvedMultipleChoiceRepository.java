package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolvedMultipleChoiceRepository extends JpaRepository<SolvedMultipleChoice, Long> {

    List<SolvedMultipleChoice> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    @Query("select distinct s.questionId from SolvedMultipleChoice s where s.userId = :userId")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);
}
