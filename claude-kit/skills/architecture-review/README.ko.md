# Architecture Review 스킬 (한글 번역본)

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

> Java 프로젝트 구조·패키지·의존성 방향에 대한 매크로 레벨 분석.

## 무엇을 하는가

프로젝트 아키텍처를 상위 레벨에서 분석합니다:
- 패키지 구성 방식 (by-layer / by-feature)
- 레이어 간 의존성 방향 (Controller → Service → Mapper)
- 모듈 경계와 결합도
- 아키텍처 안티패턴 (god 패키지, 레이어 경계 누출 등)

> **스택 전제 (ILIS)**: Spring Boot 3.x, Java 21, **MyBatis**(JPA 미사용, SQL은 XML), PostgreSQL, 전통 레이어드 MVC. 레이어 경계·단방향 의존(Controller → Service → Mapper)·순환 금지·god 패키지 금지는 스택과 무관하게 항상 적용됩니다.

## 언제 사용하는가

- "이 프로젝트의 아키텍처 리뷰해줘"
- "이 패키지 구조 괜찮아?"
- "레이어 경계(Controller / Service / Mapper) 잘 지켜지는지 봐줘"
- "아키텍처 위반 사항 찾아줘"
- 대규모 리팩토링 전

## 핵심 개념

### 패키지 전략

| 전략 | 적합한 경우 | 트레이드오프 |
|----------|----------|-----------|
| By-layer | 소규모, 빠른 시작 | 관련 코드가 흩어짐 |
| By-feature | 중규모, 명확한 모듈 | 공유 커널 필요 |

### 의존성 방향

```
Controller → Service → Mapper (데이터 접근)

규칙: 의존은 상위→하위 단방향으로만 흐른다 (Controller → Service → Mapper).
Service는 Controller나 웹/HTTP 타입을 import하면 안 되고,
Controller는 Service를 건너뛰고 Mapper를 직접 호출하면 안 된다.
```

## 사용 예시

```
사용자: 이 프로젝트 아키텍처 리뷰해줘

Claude: [패키지 구조 분석]
        [의존성 방향 점검]
        [위반 사항 식별]
        [우선순위 기반 권고 제시]
```

## 점검 항목

1. **패키지 구조** — 구성 방식, 명명 일관성
2. **의존성 방향** — 단방향 흐름(상위→하위: Controller → Service → Mapper), 역방향/상위 누출 여부
3. **레이어 경계** — 적절한 관심사 분리
4. **모듈 경계** — 명확한 API, 캡슐화
5. **확장성** — 기능을 별도 모듈로 분리 가능한가

## 관련 스킬

- `solid-principles` — 클래스 레벨 설계 (이 스킬은 패키지·모듈 레벨)
- `design-patterns` — 구현 패턴 (이 스킬은 구조적)
- `clean-code` — 코드 품질 (이 스킬은 아키텍처 품질)

## 참고 자료

- [Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) — 의존 방향 개념의 배경(참고용, ILIS는 전통 MVC)
- [Package by Feature](https://phauer.com/2020/package-by-feature/)
