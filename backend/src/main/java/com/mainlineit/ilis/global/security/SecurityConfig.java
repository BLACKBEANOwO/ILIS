package com.mainlineit.ilis.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 보안 설정 — <b>세션 방식</b>(관리시스템). JWT를 쓰지 않는다.
 *
 * <p>인증 후 상태는 서버 세션(JSESSIONID 쿠키)으로 유지한다. 편집시스템(JWT)과 별개 배포이며,
 * 향후 SSO 도입 시 두 시스템은 동일 IdP의 클라이언트로 붙되 세션/JWT 유지 방식은 각자 독립이다.
 * (배경·결정 근거: {@code .claude/context/security-policy.md})
 *
 * <p><b>현재는 프로젝트 초기 골격이라 실제 로그인·인가를 구현하지 않았다.</b>
 * 아래 TODO를 로그인/SSO 도입 시점에 처리한다.
 *
 * @author mainlineit
 * @since 0.0.1
 * @version 0.0.1
 */
@Configuration
@EnableMethodSecurity   // @PreAuthorize 역할/데이터 기준 인가 (backend/CLAUDE.md §권한 판정)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 세션 posture: 필요 시 세션 생성(무상태 아님)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // API 문서(springdoc) — authenticated 전환 후에도 접근 유지 대상
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // TODO(로그인/SSO 도입): 공개 경로 외 .anyRequest().authenticated() 로 전환
                        //   - /api/auth/** (로그인·로그아웃) permitAll
                        //   - 그 외 authenticated + @PreAuthorize 로 역할/데이터 인가
                        .anyRequest().permitAll())
                // 폼 로그인·httpBasic 기본 UI 비활성 (SPA + JSON API. 로그인은 추후 커스텀 엔드포인트)
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                // TODO(로그인 도입): CSRF 쿠키 토큰 방식으로 활성화(SPA 표준). 현재는 변경 엔드포인트가 없어 비활성.
                .csrf(csrf -> csrf.disable());
        return http.build();
    }
}
