package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnswerChoiceRepository extends JpaRepository<AnswerChoice, Long> {

    List<AnswerChoice> findByQuestionIdOrderBySequence(Long questionId);

    Optional<AnswerChoice> findFirstByQuestionIdAndIsCorrectTrue(Long questionId);

    // 오답 해설은 사용자가 자주 하는 오해의 카탈로그다. 태그가 있으면 태그로 좁혀 뽑는다.
    @Query("""
            select c.explanation
            from AnswerChoice c
            where c.isCorrect = false
              and c.explanation is not null
              and c.questionId in (
                  select qt.questionId
                  from QuestionTag qt, Tag t
                  where qt.tagId = t.id and t.name in :tagNames
              )
            order by c.id
            """)
    List<String> findWrongExplanationsByTagNames(@Param("tagNames") List<String> tagNames, Pageable pageable);

    @Query("""
            select c.explanation
            from AnswerChoice c
            where c.isCorrect = false
              and c.explanation is not null
              and c.questionId in (select q.id from Question q where q.category = :category)
            order by c.id
            """)
    List<String> findWrongExplanationsByCategory(@Param("category") Category category, Pageable pageable);
}
