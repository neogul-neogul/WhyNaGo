package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    @Query("""
            select q
            from Question q
            where q.type = com.neogul.whynago.question.domain.QuestionType.MULTIPLE_CHOICE
              and q.id not in (
                  select ac.relatedQuestionId
                  from AnswerChoice ac
                  where ac.relatedQuestionId is not null
              )
              and (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            order by q.id desc
            """)
    List<Question> findRootMultipleChoices(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("category") Category category,
            @Param("keyword") String keyword
    );
}
