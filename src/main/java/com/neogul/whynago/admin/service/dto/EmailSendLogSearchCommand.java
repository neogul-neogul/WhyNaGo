package com.neogul.whynago.admin.service.dto;

import com.neogul.whynago.emailbatch.domain.EmailSendStatus;

public record EmailSendLogSearchCommand(
        Long executionId,
        // null이면 성공·실패를 모두 조회한다("실패만 보기" 필터가 꺼진 상태)
        EmailSendStatus status,
        int page,
        int size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static EmailSendLogSearchCommand of(Long executionId, EmailSendStatus status, Integer page, Integer size) {
        return new EmailSendLogSearchCommand(executionId, status, normalizePage(page), normalizeSize(size));
    }

    private static int normalizePage(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    private static int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
