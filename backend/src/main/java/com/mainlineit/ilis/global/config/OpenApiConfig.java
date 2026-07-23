package com.mainlineit.ilis.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI(Swagger) 문서 설정. UI 경로: {@code /swagger-ui.html}, 스펙: {@code /v3/api-docs}.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ilisOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("ILIS API")
                .description("Indonesia Legislation Information System - 관리시스템 API")
                .version("v0.0.1"));
    }
}
