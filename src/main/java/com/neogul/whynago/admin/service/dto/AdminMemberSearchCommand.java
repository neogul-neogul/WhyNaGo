package com.neogul.whynago.admin.service.dto;

public record AdminMemberSearchCommand(
        String keyword,
        int page,
        int size
) {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    public static AdminMemberSearchCommand of(String keyword, Integer page, Integer size) {
        return new AdminMemberSearchCommand(keyword, normalizePage(page), normalizeSize(size));
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
