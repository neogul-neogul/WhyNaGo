package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedSessionRepository extends JpaRepository<SolvedSession, Long> {

    List<SolvedSession> findByUserIdOrderBySolvedAtDesc(Long userId, Pageable pageable);

    List<SolvedSession> findByUserIdAndSolvedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);
}
