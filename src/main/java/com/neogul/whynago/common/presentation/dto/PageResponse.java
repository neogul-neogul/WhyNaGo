package com.neogul.whynago.common.presentation.dto;

import java.util.List;

// 페이징 메타는 도메인 지식이 아니라 전송 규격이라 common에 둔다.
// 도메인마다 같은 필드의 응답을 따로 만들지 않도록 제네릭으로 공유한다.
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages, page + 1 >= totalPages);
    }
}
