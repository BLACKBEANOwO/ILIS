# Concurrency Review 스킬 (한글 번역본)

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

> Java 동시성 코드의 스레드 안전성, 경합 조건, 현대 패턴을 검토합니다.

## 무엇을 하는가

다중 스레드 Java 코드를 다음 관점에서 리뷰합니다:
- 경합 조건(race condition), 가시성(visibility) 이슈
- 데드락 가능성
- 현대 패턴 (Virtual Threads, Structured Concurrency)
- Spring `@Async` 함정
- `CompletableFuture` 에러 처리
- 스레드 풀 구성

## 왜 중요한가

> 다중 스레드 애플리케이션의 약 60%는 공유 자원의 부적절한 관리로 인해 문제를 겪습니다.

동시성 버그는 재현·테스트·디버깅이 모두 어렵습니다. 운영에서 발견하는 것보다 코드 리뷰 단계에서 잡는 것이 훨씬 낫습니다.

## 언제 사용하는가

- "이 코드 스레드 안전성 봐줘"
- "동시성 문제 점검해줘"
- "이 비동기 코드 맞아?"
- `synchronized`, `volatile`, `@Async` 가 있는 코드 리뷰
- `CompletableFuture` 또는 `ExecutorService` 사용 점검 시

## 다루는 주제

### 모던 Java (21/25)
| 주제 | 점검 포인트 |
|-------|---------------|
| Virtual Threads | I/O 바운드용으로 사용, CPU 바운드에는 부적합 |
| Structured Concurrency | 적절한 스코프 관리 |
| ScopedValue | ThreadLocal 대신 사용 권장 |

### Spring `@Async`
| 함정 | 문제점 |
|---------|-------|
| 같은 클래스 내부 호출 | 프록시 우회로 동기 실행됨 |
| non-public 메서드 | 프록시가 가로채지 못함 |
| 기본 executor | 작업당 스레드 생성 (OOM 위험) |
| SecurityContext | ThreadLocal이 전파되지 않음 |

### 전통적 이슈
| 문제 | 예시 |
|-------|---------|
| 경합 조건 | 동기화 없는 check-then-act |
| 가시성 | volatile 누락 |
| 데드락 | 락 획득 순서 불일치 |

## 사용 예시

```
사용자: 이 서비스의 스레드 안전성 리뷰해줘

Claude: [공유 가변 상태 점검]
        [동기화 검증]
        [@Async 구성 리뷰]
        [CompletableFuture 에러 처리 점검]
        [필요 시 모던 대안 제안]
```

## 심각도 등급

| 등급 | 의미 |
|-------|---------|
| 🔴 High | 실제 버그 가능성 — 경합 조건, 데드락 위험 |
| 🟡 Medium | 잠재 이슈 — 측정/검증 필요 |
| 🟢 Modern | Java 21/25 패턴 적용 기회 |

## 관련 스킬

- `performance-smell-detection` — 성능 이슈 (스레드 안전성과는 별개)
- `java-code-review` — 일반 코드 리뷰 (기본 동시성 포함)
- `spring-boot-patterns` — Spring 패턴 (기초적인 `@Async` 포함)

## 참고 자료

- [Java Concurrency Code Review Checklist](https://github.com/code-review-checklists/java-concurrency)
- [Baeldung - Common Concurrency Pitfalls](https://www.baeldung.com/java-common-concurrency-pitfalls)
- [Oracle - Virtual Threads](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [JavaPro - Java 25 Virtual Threads](https://javapro.io/2025/12/23/java-25-getting-the-most-out-of-virtual-threads-with-structured-task-scopes-and-scoped-values/)
- [Spring @Async Problems](https://serdaralkancode.medium.com/problems-and-solutions-when-using-async-in-spring-boot-e383f9d3b45d)
- 도서: "Java Concurrency in Practice" by Brian Goetz
