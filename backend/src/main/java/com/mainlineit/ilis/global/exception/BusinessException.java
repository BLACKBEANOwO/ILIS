package com.mainlineit.ilis.global.exception;

import lombok.Getter;

/**
 * 업무 규칙에 의한 거절 — 요청 자체는 정상 처리된 것이므로 HTTP 200 + status=FAIL 로 응답된다.
 * (상태 전이 불가·중복 등록·기한 경과 등. 권한은 해당하지 않는다 — Security 계층에서 403)
 *
 * <p><b>문구를 담지 않는다.</b> code 만 들고 다니고, 사용자에게 보일 문구는
 * 전역 핸들러가 {@code MessageSource} 에서 요청 언어에 맞춰 조회한다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final Object[] args;      // 메시지에 끼워 넣을 값 (선택)

    public BusinessException(String code, Object... args) {
        super(code);                  // 로그용 — 사용자에게 나가지 않는다
        this.code = code;
        this.args = args;
    }
}
