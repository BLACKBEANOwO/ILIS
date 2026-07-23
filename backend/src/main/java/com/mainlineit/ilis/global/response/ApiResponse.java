package com.mainlineit.ilis.global.response;

import java.util.List;

/**
 * 공통 응답 포맷. 성공·실패 모두 이 타입으로 감싼다.
 *
 * <p>{@code status}/{@code code}는 업무 결과를, HTTP 상태 코드는 요청이 처리되었는가를 나타낸다.
 * 둘은 별개이며 일치하지 않을 수 있다(업무 규칙 거절은 HTTP 200 + status=FAIL).
 *
 * <p>조립은 정적 팩토리로만 한다. Controller에서 직접 {@code new} 하지 않는다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
public record ApiResponse<T>(
        String status,              // SUCCESS | FAIL
        String code,                // OK | INVALID_INPUT | NOT_FOUND | SERVER_ERROR ...
        String message,
        T data,
        List<ErrorDetail> errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "OK", "요청이 성공했습니다.", data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>("FAIL", code, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message, List<ErrorDetail> errors) {
        return new ApiResponse<>("FAIL", code, message, null, errors);
    }
}
