package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 문제은행 목록은 유형·본질문/꼬리질문 구분 없이 조건에 맞는 모든 Question을 노출한다.
    // 어떤 보기의 relatedQuestionId로 참조되는지 여부는 노출에 영향을 주지 않는다.
    @Query("""
            select q
            from Question q
            where (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            order by q.id desc
            """)
    List<Question> findQuestions(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("category") Category category,
            @Param("keyword") String keyword
    );

    @Query("""
            select q.category as category, count(q) as total
            from Question q
            group by q.category
            """)
    List<CategoryQuestionCount> countGroupByCategory();
}
