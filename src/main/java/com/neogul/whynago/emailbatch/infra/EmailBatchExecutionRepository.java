package com.neogul.whynago.emailbatch.infra;

import com.neogul.whynago.emailbatch.domain.EmailBatchExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailBatchExecutionRepository extends JpaRepository<EmailBatchExecution, Long> {

    Page<EmailBatchExecution> findAllByOrderByExecutedAtDesc(Pageable pageable);
}
