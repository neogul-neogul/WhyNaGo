package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EssaySolvedRepository extends JpaRepository<EssaySolved, Long> {

    List<EssaySolved> findBySolvedSessionIdOrderBySequence(Long solvedSessionId);

    // 꼬리질문은 AI가 생성해 참조할 Question이 없으므로(questionId is null) 제외한다.
    @Query("select distinct e.questionId from EssaySolved e where e.userId = :userId and e.questionId is not null")
    List<Long> findSolvedQuestionIds(@Param("userId") Long userId);

    List<EssaySolved> findByUserIdAndType(Long userId, ItemType type);

    // 문항 통계 배치용 전역 집계다. 꼬리질문은 questionId가 없어 집계 대상이 아니다.
    @Query("""
            select e.questionId as questionId,
                   count(e) as solvedCount,
                   sum(case when e.isCorrect = true then 1L else 0L end) as correctCount,
                   avg(e.elapsedSeconds) as avgElapsedSeconds
            from EssaySolved e
            where e.questionId is not null
            group by e.questionId
            """)
    List<QuestionSolveCount> aggregateByQuestion();
}
