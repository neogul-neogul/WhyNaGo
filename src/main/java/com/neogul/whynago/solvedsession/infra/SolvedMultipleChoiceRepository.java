package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.dto.ChoiceSelectionCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveSummary;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SolvedMultipleChoiceRepository extends JpaRepository<SolvedMultipleChoice, Long> {

    List<SolvedMultipleChoice> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    @Query("select distinct s.questionId from SolvedMultipleChoice s where s.userId = :userId")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);

    // 같은 문항이 세션에 따라 본질문이기도 꼬리질문이기도 하므로 ItemType으로 나누지 않는다.
    // avg·count(elapsedSeconds)는 null을 세지 않으므로 소요 시간 수집 전에 쌓인 응답이 평균을 희석하지 않는다.
    @Query("""
            select count(s) as totalCount,
                   sum(case when s.isCorrect = true then 1 else 0 end) as correctCount,
                   avg(s.elapsedSeconds) as averageElapsedSeconds,
                   count(s.elapsedSeconds) as elapsedSampleCount
            from SolvedMultipleChoice s
            where s.questionId = :questionId
            """)
    QuestionSolveSummary findSolveSummary(@Param("questionId") Long questionId);

    @Query("""
            select s.userChoiceId as choiceId, count(s) as selectedCount
            from SolvedMultipleChoice s
            where s.questionId = :questionId
            group by s.userChoiceId
            """)
    List<ChoiceSelectionCount> countGroupByUserChoice(@Param("questionId") Long questionId);

    // 관리자 문제 목록의 풀이수·정답률 컬럼용 벌크 집계. 단건 조회(findSolveSummary)와 달리 여러 문제를 한 번에 묶는다.
    @Query("""
            select s.questionId as questionId,
                   count(s) as totalCount,
                   sum(case when s.isCorrect = true then 1 else 0 end) as correctCount
            from SolvedMultipleChoice s
            where s.questionId in :questionIds
            group by s.questionId
            """)
    List<QuestionSolveCount> countGroupByQuestion(@Param("questionIds") List<Long> questionIds);
}
