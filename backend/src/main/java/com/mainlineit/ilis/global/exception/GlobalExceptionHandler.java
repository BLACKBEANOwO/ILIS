package com.mainlineit.ilis.global.exception;

import com.mainlineit.ilis.global.response.ApiResponse;
import com.mainlineit.ilis.global.response.ErrorDetail;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외 → 공통 응답 포맷 변환을 한곳에 모은다.
 * 사용자에게 보일 문구는 {@link MessageSource}에서 요청 언어(Accept-Language)에 맞춰 조회한다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // 대상 부재 — 자원 부재 관례에 따라 404 (업무 거절 200 이 아님)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, Locale locale) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail("NOT_FOUND", messageSource.getMessage("error.NOT_FOUND", null, locale)));
    }

    // 업무 규칙 거절 — 요청은 정상 처리됨 → HTTP 200
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, Locale locale) {
        log.info("Business rule rejected: {}", ex.getCode());
        String message = messageSource.getMessage("error." + ex.getCode(), ex.getArgs(), locale);
        return ResponseEntity.ok(ApiResponse.fail(ex.getCode(), message));
    }

    // 요청 형식 오류 — 업무 로직 도달 못 함 → HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, Locale locale) {
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> new ErrorDetail(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("INVALID_INPUT",
                        messageSource.getMessage("error.INVALID_INPUT", null, locale), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, Locale locale) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("SERVER_ERROR",
                        messageSource.getMessage("error.SERVER_ERROR", null, locale)));
    }
}
