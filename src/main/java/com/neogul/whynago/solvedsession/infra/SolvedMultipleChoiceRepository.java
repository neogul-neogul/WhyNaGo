package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedMultipleChoiceRepository extends JpaRepository<SolvedMultipleChoice, Long> {

    List<SolvedMultipleChoice> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);
}
