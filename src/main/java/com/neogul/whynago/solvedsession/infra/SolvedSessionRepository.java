package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.dto.TypeSolveCount;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolvedSessionRepository extends JpaRepository<SolvedSession, Long> {

    List<SolvedSession> findByUserIdOrderBySolvedAtDesc(Long userId, Pageable pageable);

    List<SolvedSession> findByUserIdAndSolvedAtBetween(Long userId, LocalDateTime from, LocalDateTime to);

    // 아래 세 집계는 특정 사용자가 아니라 전체 사용자를 대상으로 한다(관리자 대시보드).
    @Query("select sum(s.totalCount) from SolvedSession s where s.solvedAt between :from and :to")
    Long sumQuestionCountBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("select s.type as type, sum(s.totalCount) as questionCount from SolvedSession s group by s.type")
    List<TypeSolveCount> sumQuestionCountGroupByType();

    @Query("select count(distinct s.userId) from SolvedSession s where s.solvedAt between :from and :to")
    long countActiveUsersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
