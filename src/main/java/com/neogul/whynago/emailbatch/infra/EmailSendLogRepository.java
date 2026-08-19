package com.neogul.whynago.emailbatch.infra;

import com.neogul.whynago.emailbatch.domain.EmailSendLog;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.infra.dto.FailureReasonCount;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailSendLogRepository extends JpaRepository<EmailSendLog, Long> {

    Page<EmailSendLog> findAllByBatchExecutionIdOrderBySentAtAsc(Long batchExecutionId, Pageable pageable);

    Page<EmailSendLog> findAllByBatchExecutionIdAndStatusOrderBySentAtAsc(
            Long batchExecutionId,
            EmailSendStatus status,
            Pageable pageable
    );

    // 발송 상세 목록은 페이징되므로 실패 사유 요약은 전체를 대상으로 따로 집계한다.
    @Query("""
            select l.failureReason as reason, count(l) as sendCount
            from EmailSendLog l
            where l.batchExecutionId = :batchExecutionId and l.status = :status
            group by l.failureReason
            order by count(l) desc
            """)
    List<FailureReasonCount> countGroupByFailureReason(
            @Param("batchExecutionId") Long batchExecutionId,
            @Param("status") EmailSendStatus status
    );
}
