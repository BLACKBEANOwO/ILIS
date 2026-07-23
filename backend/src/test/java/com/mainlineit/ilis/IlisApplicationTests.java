package com.mainlineit.ilis;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

/**
 * 애플리케이션 컨텍스트 로딩 테스트. Testcontainers PostgreSQL로 실제 datasource·Flyway까지 검증한다.
 * (실행에 Docker 필요)
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class IlisApplicationTests {

    @Test
    void contextLoads() {
    }
}
