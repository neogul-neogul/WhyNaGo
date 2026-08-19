package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.infra.dto.QuestionTagName;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionTagRepository extends JpaRepository<QuestionTag, Long> {

    List<QuestionTag> findByQuestionIdIn(List<Long> questionIds);

    // 부여 순서를 유지한다. 첫 행이 그 문항의 주 태그라는 시드 규칙이 조회에도 그대로 보여야 한다.
    @Query("""
            select qt.questionId as questionId, t.name as name
            from QuestionTag qt, Tag t
            where qt.tagId = t.id
              and qt.questionId in :questionIds
            order by qt.id
            """)
    List<QuestionTagName> findTagNames(@Param("questionIds") List<Long> questionIds);
}
