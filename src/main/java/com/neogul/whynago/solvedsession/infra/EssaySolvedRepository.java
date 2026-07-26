package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EssaySolvedRepository extends JpaRepository<EssaySolved, Long> {

    List<EssaySolved> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);
}
