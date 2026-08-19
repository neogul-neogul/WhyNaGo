package com.neogul.whynago.mastery.infra;

import com.neogul.whynago.mastery.domain.MasteryRecord;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.infra.dto.CategoryMasteryCount;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MasteryRecordRepository extends JpaRepository<MasteryRecord, Long> {

    // 같은 문항을 여러 번 풀면 최신 판정만 쓴다.
    Optional<MasteryRecord> findFirstByUserIdAndQuestionIdOrderByIdDesc(Long userId, Long questionId);

    // id 오름차순이라 같은 문항이 여러 번 나오면 뒤에 오는 행이 최신이다.
    List<MasteryRecord> findByUserIdAndSourceAndQuestionIdInOrderByIdAsc(
            Long userId, MasterySource source, List<Long> questionIds);

    List<MasteryRecord> findByUserIdOrderByIdDesc(Long userId);

    // 카테고리별 판정 분포다. 태그가 없는 문항의 판정도 카테고리 신호로 포함된다.
    @Query("""
            select r.category as category, cast(r.level as string) as level, count(r) as count
            from MasteryRecord r
            where r.userId = :userId
            group by r.category, r.level
            """)
    List<CategoryMasteryCount> countByCategoryAndLevel(@Param("userId") Long userId);
}
