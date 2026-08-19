package com.neogul.whynago.question.infra;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // 문제은행 목록은 유형·본질문/꼬리질문 구분 없이 조건에 맞는 모든 Question을 노출한다.
    // 어떤 보기의 relatedQuestionId로 참조되는지 여부는 노출에 영향을 주지 않는다.
    //
    // 단 검수를 통과하지 않은 문항(PENDING·REJECTED)은 제외한다. 판별 기준이 source가 아니라
    // reviewStatus인 이유는, 승인된 생성 문항은 시드 문항과 완전히 동등하게 노출되기 때문이다.
    // 파라미터가 아니라 하드코딩인 이유는 오늘의 면접 질문을 뽑는 DailyQuestionResolver도
    // 이 쿼리를 쓰기 때문이다 — 파라미터로 열어두면 호출자가 빠뜨릴 수 있다.
    @Query(value = """
            select q
            from Question q
            where (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and q.reviewStatus = com.neogul.whynago.question.domain.QuestionReviewStatus.APPROVED
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            order by q.id desc
            """,
            countQuery = """
            select count(q)
            from Question q
            where (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and q.reviewStatus = com.neogul.whynago.question.domain.QuestionReviewStatus.APPROVED
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            """)
    Page<Question> findQuestions(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("category") Category category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 관리자 문제 관리 화면용이다. 위 findQuestions와 조건은 같지만 review_status를 보지 않는다 —
    // 검수 대기(PENDING) 문항을 봐야 하는 유일한 화면이라 노출 게이트를 통과시킨다.
    // 같은 쿼리에 파라미터를 다는 대신 메서드를 나눈 이유는, 필터를 열어두면 사용자 화면 호출자가
    // 값을 빠뜨렸을 때 검수 전 문항이 조용히 새어 나가기 때문이다.
    @Query(value = """
            select q
            from Question q
            where (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            order by q.id desc
            """,
            countQuery = """
            select count(q)
            from Question q
            where (:type is null or q.type = :type)
              and (:difficulty is null or q.difficulty = :difficulty)
              and (:category is null or q.category = :category)
              and (:keyword is null or lower(q.title) like lower(concat('%', :keyword, '%'))
                   or lower(q.content) like lower(concat('%', :keyword, '%')))
            """)
    Page<Question> findQuestionsForAdmin(
            @Param("type") QuestionType type,
            @Param("difficulty") Difficulty difficulty,
            @Param("category") Category category,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 진척도 화면의 카테고리별 분모다. 검수 전 문항이 섞이면 아무것도 하지 않았는데 분모가
    // 늘어나므로 목록과 같은 기준으로 APPROVED만 센다. 승인된 생성 문항은 분모에 포함된다.
    @Query("""
            select q.category as category, count(q) as total
            from Question q
            where q.reviewStatus = com.neogul.whynago.question.domain.QuestionReviewStatus.APPROVED
            group by q.category
            """)
    List<CategoryQuestionCount> countGroupByCategory();

    // 중복 생성을 막기 위한 네거티브 컨텍스트다. 검수 전 문항도 이미 만들어진 주제이므로 함께 본다.
    @Query("""
            select q.title
            from Question q
            where q.type = com.neogul.whynago.question.domain.QuestionType.ESSAY
              and q.reviewStatus <> com.neogul.whynago.question.domain.QuestionReviewStatus.REJECTED
              and (q.category = :category
                   or q.id in (
                       select qt.questionId
                       from QuestionTag qt, Tag t
                       where qt.tagId = t.id and t.name in :tagNames
                   ))
            """)
    List<String> findEssayTitles(@Param("category") Category category, @Param("tagNames") List<String> tagNames);

    // AI 생성이 불가능할 때 쓰는 폴백 후보다. 이때는 객관식·서술형을 모두 후보로 삼는다.
    @Query("""
            select q
            from Question q
            where q.reviewStatus = com.neogul.whynago.question.domain.QuestionReviewStatus.APPROVED
              and q.category in :categories
            order by q.difficulty, q.id
            """)
    List<Question> findApprovedByCategories(@Param("categories") List<Category> categories, Pageable pageable);

    // 콜드스타트용이다. 이력이 없는 사용자에게는 난이도 하 문항을 카테고리별로 고르게 준다.
    @Query("""
            select q
            from Question q
            where q.reviewStatus = com.neogul.whynago.question.domain.QuestionReviewStatus.APPROVED
              and q.difficulty = :difficulty
            order by q.category, q.id
            """)
    List<Question> findApprovedByDifficulty(@Param("difficulty") Difficulty difficulty);
}
