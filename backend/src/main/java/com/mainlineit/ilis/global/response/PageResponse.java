package com.mainlineit.ilis.global.response;

import java.util.List;

/**
 * 목록 조회의 페이지 응답. {@link ApiResponse}의 {@code data}에 담아 사용한다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}
