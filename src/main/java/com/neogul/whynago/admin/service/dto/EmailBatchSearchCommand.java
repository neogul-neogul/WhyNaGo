package com.neogul.whynago.admin.service.dto;

public record EmailBatchSearchCommand(
        int page,
        int size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static EmailBatchSearchCommand of(Integer page, Integer size) {
        return new EmailBatchSearchCommand(normalizePage(page), normalizeSize(size));
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
