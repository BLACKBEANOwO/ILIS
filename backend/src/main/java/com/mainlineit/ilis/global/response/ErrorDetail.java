package com.mainlineit.ilis.global.response;

/**
 * 필드 단위 오류 상세. 입력 검증 실패 시 {@link ApiResponse#errors()}에 담긴다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
public record ErrorDetail(String field, Object rejectedValue, String message) {
}
