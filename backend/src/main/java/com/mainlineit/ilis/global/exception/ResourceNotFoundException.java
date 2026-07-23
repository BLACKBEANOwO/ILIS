package com.mainlineit.ilis.global.exception;

/**
 * 대상 자원이 존재하지 않을 때. 자원 부재 관례에 따라 HTTP 404로 응답된다(업무 거절 200이 아님).
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}
