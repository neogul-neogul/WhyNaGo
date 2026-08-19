package com.neogul.whynago.solvedsession.infra;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
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

    // 관리자 문제 목록의 풀이수·정답률 컬럼용 벌크 집계. 꼬리질문은 questionId가 null이라 in절에 걸리지 않아 자동 제외된다.
    @Query("""
            select e.questionId as questionId,
                   count(e) as totalCount,
                   sum(case when e.isCorrect = true then 1 else 0 end) as correctCount
            from EssaySolved e
            where e.questionId in :questionIds
            group by e.questionId
            """)
    List<QuestionSolveCount> countGroupByQuestion(@Param("questionIds") List<Long> questionIds);
}
