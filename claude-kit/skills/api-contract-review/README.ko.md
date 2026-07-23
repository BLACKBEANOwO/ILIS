# API Contract Review 스킬 (한글 번역본)

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

> REST API의 HTTP 의미론, 버저닝, 일관성을 점검합니다.

## 무엇을 하는가

다음 항목 중심으로 REST API 설계를 검토합니다:
- HTTP 동사 정확성 (GET vs POST vs PUT vs PATCH)
- API 버저닝 전략
- 요청/응답 구조 (DTO vs 엔티티)
- 상태 코드 사용 (오류 응답에 200을 쓰지 않기)
- 하위 호환성 이슈

## 언제 사용하는가

- "이 API 리뷰해줘" / "REST 엔드포인트 점검해줘"
- API 변경 사항을 릴리스하기 전
- 컨트롤러 PR 리뷰 시
- API가 REST 베스트 프랙티스를 따르는지 확인할 때

## 핵심 개념

### Audit vs Template (감사 vs 템플릿)

| spring-boot-patterns | api-contract-review |
|---------------------|---------------------|
| 컨트롤러를 **어떻게 작성할지** | 기존 API를 **검토** |
| 템플릿과 예시 | 체크리스트와 안티패턴 |
| 신규 코드 작성 | 기존 코드 감사 |

### 흔히 발견되는 문제

| 문제 | 예시 |
|-------|---------|
| 잘못된 동사 | 검색에 GET 대신 POST 사용 |
| 버저닝 누락 | `/v1/users` 대신 `/users` |
| 엔티티 노출 | JPA 엔티티를 그대로 응답으로 반환 |
| 200 + 오류 본문 | HTTP 200으로 `{"status": "error"}` 응답 |
| Breaking 변경 | 요청 본문에 필수 필드 추가 |

## 사용 예시

```
사용자: UserController의 API를 리뷰해줘

Claude: [HTTP 동사 사용 확인]
        [버저닝 검증]
        [엔티티 노출 여부 점검]
        [에러 처리 리뷰]
        [Breaking 변경 식별]
```

## 점검 항목

1. **HTTP 의미론** — 작업에 맞는 동사 선택
2. **URL 설계** — 버저닝, 명명 규칙
3. **요청 처리** — 검증, DTO
4. **응답 설계** — DTO, 페이지네이션, 일관성
5. **에러 처리** — 상태 코드, 에러 포맷
6. **호환성** — Breaking vs Non-breaking 변경

## 관련 스킬

- `spring-boot-patterns` — 컨트롤러 작성 템플릿 (이 스킬은 그것을 감사함)
- `security-audit` — API의 보안 측면
- `java-code-review` — 일반 코드 리뷰 (이 스킬은 API 특화)

## 참고 자료

- [REST API Design Best Practices](https://restfulapi.net/)
- [HTTP Status Codes](https://httpstatuses.com/)
- [API Versioning](https://www.baeldung.com/rest-versioning)
