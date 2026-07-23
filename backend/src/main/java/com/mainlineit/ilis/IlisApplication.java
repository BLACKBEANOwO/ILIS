package com.mainlineit.ilis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ILIS(Indonesia Legislation Information System) 백엔드 진입점.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
@SpringBootApplication
@MapperScan("com.mainlineit.ilis.domain.**.repository")
public class IlisApplication {

    public static void main(String[] args) {
        SpringApplication.run(IlisApplication.class, args);
    }
}
