package com.mainlineit.ilis;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 애플리케이션 컨텍스트 로딩 테스트.
 *
 * <p>현재는 PostgreSQL 접속(Flyway 마이그레이션 포함)이 있어야 컨텍스트가 뜨므로 비활성화되어 있다.
 * Testcontainers 도입 후 {@code @Disabled}를 제거해 활성화한다. (작업일지 TODO)
 */
@Disabled("PostgreSQL 필요 — Testcontainers 도입 후 활성화")
@SpringBootTest
class IlisApplicationTests {

    @Test
    void contextLoads() {
    }
}
