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

    // 문제은행 진입 문제(루트)만 조회한다. 다른 문제의 선택지에서 이어지는 객관식 꼬리질문은 제외되고,
    // 서술형 꼬리질문은 세션마다 AI가 생성해 Question 행이 없으므로 서술형은 모두 루트로 조회된다.
    @Query("""
            select q
            from Question q
            where q.id not in (
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
    List<Question> findRootQuestions(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("category") Category category,
            @Param("keyword") String keyword
    );
}
