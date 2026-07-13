package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolvedSessionRepository extends JpaRepository<SolvedSession, Long> {
}
