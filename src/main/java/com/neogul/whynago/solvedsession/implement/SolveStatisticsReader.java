package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.implement.dto.SolveCountByType;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// SolvedSessionReader는 사용자 단위 조회를 담당하고, 이 클래스는 전체 사용자 기준 집계를 담당한다.
@Component
@RequiredArgsConstructor
public class SolveStatisticsReader {

    private final SolvedSessionRepository solvedSessionRepository;

    public long countQuestionsBetween(LocalDateTime from, LocalDateTime to) {
        return normalize(solvedSessionRepository.sumQuestionCountBetween(from, to));
    }

    public long countActiveUsersBetween(LocalDateTime from, LocalDateTime to) {
        return solvedSessionRepository.countActiveUsersBetween(from, to);
    }

    public long countQuestionsByUser(Long userId) {
        return normalize(solvedSessionRepository.sumQuestionCountByUserId(userId));
    }

    public SolveCountByType countCumulativeByType() {
        return SolveCountByType.from(solvedSessionRepository.sumQuestionCountGroupByType());
    }

    // 집계 대상 세션이 없으면 sum()이 null이다.
    private long normalize(Long questionCount) {
        return questionCount == null ? 0L : questionCount;
    }
}
