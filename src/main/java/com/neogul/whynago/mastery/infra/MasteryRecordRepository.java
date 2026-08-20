package com.neogul.whynago.mastery.infra;

import com.neogul.whynago.mastery.domain.MasteryRecord;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.infra.dto.CategoryMasteryCount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MasteryRecordRepository extends JpaRepository<MasteryRecord, Long> {

    // id 오름차순이라 같은 문항이 여러 번 나오면 뒤에 오는 행이 최신이다.
    List<MasteryRecord> findByUserIdAndSourceAndQuestionIdInOrderByIdAsc(
            Long userId, MasterySource source, List<Long> questionIds);

    // 카테고리별 판정 분포다. 태그가 없는 문항의 판정도 카테고리 신호로 포함된다.
    //
    // 세는 단위는 **문항**이다. 한 번의 채점이 문항의 태그 개수만큼 행을 만들기 때문에
    // 행을 그냥 세면 태그 3개짜리 문항 하나가 3으로 잡힌다. distinct questionId로 그 중복을 없앤다.
    // 꼬리질문 판정은 제외한다 — 본질문 하나가 최대 3턴을 만들어 같은 문항이 턴 수만큼 다시 세어진다.
    @Query("""
            select r.category as category,
                   cast(r.level as string) as level,
                   count(distinct r.questionId) as count
            from MasteryRecord r
            where r.userId = :userId
              and r.source <> com.neogul.whynago.mastery.domain.MasterySource.AI_ESSAY_FOLLOWUP
            group by r.category, r.level
            """)
    List<CategoryMasteryCount> countByCategoryAndLevel(@Param("userId") Long userId);
}
