# Logging Patterns (한글 번역본)

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

**Load**: `view .claude/skills/logging-patterns/SKILL.md`

---

## 설명

SLF4J, 구조화 로깅(JSON), 요청 추적용 MDC 기반 Java 로깅 베스트 프랙티스. Claude Code 분석에 최적화된 AI-friendly 로그 포맷도 포함합니다.

---

## 사용 사례

- "이 서비스에 로깅 추가해줘"
- "이 흐름 디버그해줘" (AI가 로그 읽음)
- "구조화 로깅 셋업해줘"
- "왜 이 요청이 실패해?" (로그 분석)
- "요청 추적 추가해줘"

---

## 핵심 인사이트: AI를 위해서는 JSON

**JSON 로그가 AI/Claude Code 분석에 더 적합합니다:**

| 측면 | 텍스트 로그 | JSON 로그 |
|--------|-----------|-----------|
| 파싱 | 정규식 해석 필요 | 필드 직접 접근 |
| 토큰 사용 | 더 많음 | 더 적음 |
| 필터링 | grep 패턴 | jq 쿼리 |

```bash
# AI가 JSON을 쉽게 필터링 가능
cat app.log | jq 'select(.requestId == "abc123")'
```

---

## 다루는 주제

| 주제 | 설명 |
|-------|-------------|
| **AI-friendly 로깅** | Claude Code에 최적화된 JSON 포맷 |
| **Spring Boot 3.4+** | 네이티브 구조화 로깅 지원 |
| **Logstash Encoder** | Spring Boot < 3.4 환경용 |
| **SLF4J/MDC** | 요청 컨텍스트, 상관관계 ID(correlation ID) |
| **로그 레벨** | ERROR, WARN, INFO, DEBUG 사용 시점 |
| **무엇을 로그할까** | 비즈니스 이벤트, 타이밍, 흐름 단계 |
| **무엇을 로그하지 말아야 할까** | 비밀번호, PII, 민감 데이터 |

---

## 빠른 셋업 (Spring Boot 3.4+)

```yaml
logging:
  structured:
    format:
      console: logstash
```

추가 의존성 불필요!

---

## 관련 스킬

- `spring-boot-patterns` — Spring 설정
- `jpa-patterns` — DB 로깅

---

## 참고 자료

- [Structured Logging in Spring Boot 3.4 (spring.io)](https://spring.io/blog/2024/08/23/structured-logging-in-spring-boot-3-4/)
- [Structured Logging in Spring Boot (Baeldung)](https://www.baeldung.com/spring-boot-structured-logging)
- [10 Best Practices for Logging in Java (Better Stack)](https://betterstack.com/community/guides/logging/how-to-start-logging-with-java/)
- [Booking.com - Structured Logging](https://medium.com/booking-com-development/unlocking-observability-structured-logging-in-spring-boot-c81dbabfb9e7)
- [SLF4J Manual](https://www.slf4j.org/manual.html)
