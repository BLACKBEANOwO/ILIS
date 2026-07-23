# CLAUDE.md — 백엔드 규약

적용 위치: `backend/CLAUDE.md`
근거: 메인라인 WEB/SAAS 파트 「Backend 개발가이드 v0.51」 · 「명명규칙 기본안 v0.50」 (스택 종속 항목 제외)

---

## 스택

- Spring Boot 3.x · JDK 17 이상
- MyBatis (Mapper 인터페이스 + XML) — JPA/Hibernate 미사용
- REST API (GraphQL 미사용)
- Lombok

빌드 도구(Gradle/Maven)는 미확정 (확정 경로: 출장 담당자 협의).
DBMS는 **PostgreSQL 을 작업 전제로 진행**한다 — 정식 확정은 출장 담당자 협의 결과를 따른다.

---

## 패키지 구조

최상위를 `global`(공통 기반)과 `domain`(업무)으로 2분할한다. 기술 계층별 평면 분할을 쓰지 않는다.

```
{base}                            ← 프로젝트 소유 도메인 역순 (아래 참조)
├── global
│   ├── config          [기능]Config
│   ├── exception       [예외상황]Exception, GlobalExceptionHandler
│   ├── response        ApiResponse, PageResponse, ErrorDetail
│   ├── util            Ilis[기능]Utils
│   ├── security/jwt
│   ├── domain          [기능]Enum, [기능]Const, 공통 Dto
│   ├── aop             [기능]Handler
│   └── interceptor
└── domain
    └── law             ← 업무 도메인 단위 (law, precedent, review, assign ...)
        ├── api         LawController
        ├── service     LawService
        ├── repository  LawMapper
        └── dto         LawDto, LawCreateRequest, LawResponse

src/main/resources/mapper/        LawMapper.xml
```

> **base 패키지 `{base}`** — 프로젝트가 소유 도메인의 역순(reverse-domain)으로 정한다.
> 아래 구조·명명 규약은 base와 무관하게 동일하게 적용된다.
> - **내부 시스템**(사내 서비스): 회사 도메인 역순. 예) `com.mainlineit.saas.{솔루션명}`
> - **외부 시스템**(고객·정부 등 소유): 그 시스템 도메인 역순. 예) `id.go.peraturan.ilis` (도메인 `ilis.peraturan.go.id`)
>
> 도메인 패키지명에 **Java 예약어를 쓰지 않는다** (`case` `new` `class` `int` 등은 컴파일 불가).
> 판례 도메인은 `precedent`를 쓴다.

---

## 명명 규칙

### 클래스 — PascalCase

| 역할 | 형식 | 예시 |
|---|---|---|
| Controller | `[도메인]Controller` | `LawController` |
| Service | `[도메인]Service` | `LawService` |
| Mapper | `[도메인]Mapper` | `LawMapper` |
| Config | `[기능]Config` | `SecurityConfig` |
| Exception | `[예외상황]Exception` | `ResourceNotFoundException` |
| Utils | `Ilis[기능]Utils` | `IlisStringUtils` |

### 메소드 — camelCase, 동사 시작

| 동작 | 형식 | 비고 |
|---|---|---|
| 조회(단건) | `get[대상]By[조건]` | `getLawById(Long lawId)` |
| 조회(목록·조건 1개) | `find[대상]By[조건]` | `findLawsByStatus(String status)` |
| 조회(목록·조건 없음) | `findAll[대상]` | `findAllLaws()` |
| 조회(목록·복합/선택 조건) | `find[대상]` | `findLaws(LawSearchRequest req)` |
| 생성 | `create[대상]` | |
| 수정 | `update[대상]`, `update[대상]By[조건]` | |
| 삭제 | `delete[대상]`, `delete[대상]By[조건]` | |
| 복합 CUD | `save[대상]` | |
| boolean 반환 | `is[상태]` / `has[속성]` / `can[행위]` | `isActive`, `hasPermission`, `canRetry` |

**`get`=단건, `find`=목록**을 지킨다. 메소드명만 보고 반환이 단건인지 목록인지 판별되어야 한다.

### 변수 — camelCase

- 풀 네임 우선 — `int customerCount` (O) / `int custCnt` (X)
- 도메인 용어는 축약하지 않는다 — 법령 도메인의 `Pasal`, `Ayat` 등은 원어 유지
- 의미가 명확한 표준 약어는 허용 — `userId`, `apiUrl`
- 영문 일관, 언어 혼용 금지

### 상수 — UPPER_SNAKE_CASE

용도를 접미사로 구분한다.

| 접미사 | 용도 | 예시 |
|---|---|---|
| `_KEY` | Map/Property 키 | `USER_ID_KEY` |
| `_VAL` | 값 강조 | `STATUS_VAL` |
| `_TYPE` | 타입 구분 | `PAYMENT_TYPE` |
| `_CODE` | DB·외부 코드 매핑 | `ERROR_CODE` |
| `_FLAG` | boolean 값 | `IS_ACTIVE_FLAG` |

### 기타

- 패키지: 모두 소문자
- DB 테이블·컬럼: snake_case
- API 엔드포인트: **버전 포함 + 복수형 + kebab-case** — `/api/v1/law-articles/{lawId}`

---

## 계층별 규약

### Controller

- `@RestController` + 클래스 레벨 `@RequestMapping`
- 비즈니스 로직·Mapper 직접 호출 금지 — Service로 위임
- 도메인 객체를 그대로 반환하지 않는다 — DTO로 감싼다
- 요청 바디는 `@Valid`로 검증

### Service

- **인터페이스를 만들지 않는다.** 하나의 서비스에 여러 구현체가 필요한 경우만 예외.
- `@Service` + `@RequiredArgsConstructor` + `final` 필드 주입
- 클래스에 `@Transactional(readOnly = true)`, 쓰기 메소드에만 `@Transactional`
- 트랜잭션 전파는 **`REQUIRED`(기본) · `REQUIRES_NEW`** 만 사용한다.
  `SUPPORTS` `NOT_SUPPORTED` `MANDATORY` `NEVER` `NESTED` 사용 금지.
- 일반 예외가 아니라 도메인 예외를 던진다

### Mapper (MyBatis)

- 인터페이스 `[도메인]Mapper.java` + XML `[도메인]Mapper.xml` 짝
- 단건 조회는 없으면 `null` 반환 → Service가 null 검사 후 예외 전환
- `insert`/`update`/`delete`는 영향 행 수(`int`) 반환 → Service가 0건 검증
- 다중 파라미터는 `@Param` 명시
- `#{}` 사용, `${}`는 화이트리스트 검증을 통과한 값에만
- 동적 조건은 XML의 `<if>`/`<choose>`/`<foreach>`/`<where>` — Java 문자열 조립 금지

---

## API 응답 구조

모든 응답을 **공통 응답 포맷**으로 감싼다. 이 포맷의 `status`/`code`는 **업무 결과**를, HTTP 상태 코드는 **요청이 처리되었는가**를 나타낸다. 둘은 별개이며 일치하지 않을 수 있다.

### HTTP 상태 코드 판단 기준

기준은 **요청 자체가 유효한가**와 **업무 규칙의 판정 결과가 무엇인가**를 나누는 것이다.
요청의 유효성은 네 가지로 본다 — **주소·형식·인증·대상 존재 여부**.

넷을 모두 통과한 뒤 업무 규칙이 거절한 것만 200 + `FAIL`이고, 하나라도 걸리면 4xx다.
**대상이 존재하지 않는 것은 요청이 유효하지 않은 것**으로 보므로(자원 부재 관례) 200이 아니라 404다.

| 상황 | HTTP | `status` | 예시 |
|---|---|---|---|
| 정상 처리·정상 결과 | 200 / 201 | `SUCCESS` | 조회 성공, 등록 완료 |
| 요청은 유효, 업무 규칙이 거절 | **200** | `FAIL` | 아래 닫힌 목록 참조 |
| 요청 형식·값이 잘못됨 | 400 | `FAIL` | 필수값 누락, 타입 불일치 |
| 인증 실패 | 401 | `FAIL` | 세션 없음·만료 (이 시스템은 세션 방식 — 아래 note) |
| **인가 실패 (권한 전반)** | 403 | `FAIL` | Security 계층이 차단 — 역할·데이터 기준 모두 |
| 대상이 존재하지 않음 | 404 | `FAIL` | 없는 경로, **없는 법령 ID** |
| 서버 오류 | 500 | `FAIL` | 예기치 못한 예외 |

**404는 자원 부재 관례를 따른다.** 없는 경로든 없는 법령 ID든 "요청이 가리키는 대상이 없다"는 점은 같으므로
둘 다 404이며, 업무 거절(200)로 분류하지 않는다.

### 200 + FAIL 로 내리는 경우 (닫힌 목록)

**업무 규칙에 의한 거절만 해당한다.** 아래에 없는 사례가 생기면 이 문서를 먼저 갱신한다.

- 상태 전이 불가 — 검수 미완료 건의 개시 시도, 이미 개시된 건의 수정 시도
- 중복 등록 — 이미 존재하는 법령 번호·사건번호
- 기한 경과 — 마감된 작업에 대한 요청

**권한은 이 목록에 없다.** 권한 판정은 전부 Security 계층에서 처리한다(아래 참조).

### 권한 판정은 Security 계층에서

> **note (이 프로젝트 = 관리시스템)**: 인증은 **JWT가 아니라 세션 방식**이다. 위 `security/jwt` 패키지 대신
> `global/security/SecurityConfig`(세션)를 쓴다. 상세·근거는 `.claude/context/security-policy.md`.
> 아래 인가(권한 판정) 규칙은 세션·JWT와 무관하게 그대로 적용된다.

역할 기반이든 데이터 기반이든 **권한 체크는 Service 안에서 하지 않는다.** Security 계층에서 차단하고 **403**을 내린다.

| 판정 유형 | 처리 위치 | 응답 |
|---|---|---|
| 역할 보유 여부 (관리자·검수자 등) | `@PreAuthorize("hasRole('REVIEWER')")` | 403 |
| 데이터 기준 권한 (본인 배정 문서인가) | `@PreAuthorize("@lawPermission.canEdit(#lawId)")` + `PermissionEvaluator` | 403 |

데이터 기준 판정도 메서드 시큐리티로 끌어올린다. Service 본문에 권한 분기를 넣으면
업무 로직과 인가 로직이 섞이고, 같은 판정이 여러 메소드에 흩어진다.

> **알려진 트레이드오프**: 업무 거절을 200으로 내리면 외부 클라이언트·게이트웨이·APM이 `2xx=성공`으로
> 해석할 수 있다. 이를 감수하는 대신 **응답의 `code` 기준 메트릭을 별도로 수집**해야 한다
> (HTTP 상태 코드만으로 업무 실패율을 볼 수 없다). 이 API는 ILIS 편집시스템 내부 소비를 전제로 한다.

### 공통 응답 포맷 필드

성공은 `data`, 실패는 `code`(+검증 실패 시 `errors[]`)를 채운다. (JSON·`ApiResponse`/`ErrorDetail` record 예시는 `spring-boot-patterns` 스킬)

| 필드 | 필수 | 설명 |
|---|---|---|
| `status` | Y | `SUCCESS` / `FAIL` |
| `code` | Y | `OK` `INVALID_INPUT` `NOT_FOUND` `SERVER_ERROR` 등 |
| `message` | N | 사용자 친화 문구 |
| `data` | N | 성공 시 반환 데이터 |
| `errors` | N | 필드 단위 오류 목록 (실패 시) |

- 공통 응답 포맷 생성은 `ApiResponse<T>` 정적 팩토리로 통일 — Controller마다 직접 조립하지 않는다
- 예외 → 공통 응답 포맷 변환은 `@RestControllerAdvice` 전역 핸들러 한 곳에서

### 목록 응답 — 페이징

목록은 배열을 그대로 내리지 않고 `PageResponse<T>`(`content` · `page`(0-based) · `size` · `totalElements` · `totalPages`)로 감싼다. 합치면 `ApiResponse<PageResponse<T>>`가 된다. 페이징이 없는 짧은 목록(코드 목록 등)은 배열을 그대로 내려도 된다. **같은 엔드포인트가 상황에 따라 배열과 `PageResponse`를 번갈아 내리지 않는다.** (record·JSON 예시는 `spring-boot-patterns` 스킬)

### 날짜·시각 표기

| 자바 타입 | JSON 표기 | 예시 |
|---|---|---|
| `LocalDate` | `yyyy-MM-dd` 문자열 | `"2026-07-20"` |
| `LocalDateTime` | ISO 8601 문자열 (오프셋 없음) | `"2026-07-20T14:30:00"` |
| `OffsetDateTime` | ISO 8601 오프셋 포함 | `"2026-07-20T14:30:00+07:00"` |

숫자 타임스탬프(epoch)를 쓰지 않는다. 시간대 정책(WIB 등)은 미확정이다.

### 메시지 다국어화 — `Accept-Language`

프론트는 모든 요청에 `Accept-Language`(`id` | `en` | `ko`)를 실어 보낸다.
백엔드는 그 언어로 **번역된 문구**를 응답의 `message`·`errors[].message`에 담아 내린다.
프론트는 받은 문구를 그대로 표시한다.

**사용자에게 보이는 문구를 자바 코드에 하드코딩하지 않는다.** 예외는 `code`만 들고 다니고,
문구는 전역 핸들러가 `MessageSource`에서 조회한다.

- 리소스는 `messages/messages.properties`(기본=`id`) · `messages_en` · `messages_ko` 세 파일. `AcceptHeaderLocaleResolver`로 요청 헤더 언어를 고르고, 전역 핸들러가 `code`로 문구를 조회한다. (설정·핸들러 코드는 `spring-boot-patterns` 스킬)
- Bean Validation 메시지도 `{validation.lawNo.required}` 형태로 키를 참조한다
- **메시지 키가 없으면 예외가 나도록** 둔다 (fallback 문구로 조용히 넘어가면 누락을 못 찾는다)
- 3개 언어 리소스를 **동시에 채운다**. 한 언어만 채우고 나머지를 비우지 않는다

---

## 주석

JavaDoc 기준. 상세 규약과 템플릿은 `java-comment-style` 스킬을 따른다.

- **클래스**: 역할 1줄 + 주요 기능 + `@author` + `@since` + `@version` (필수)
- **메소드**: 설명 + `@param` + `@return` (필수), `@throws`는 선택
- 위 필수 태그에 더해 `호출처:` · `흐름:` 섹션을 둔다
- **예외** — Mapper 인터페이스 메소드는 `흐름:`을 쓰지 않는다. 실제 처리 흐름이 XML에 있어
  Java 주석에 쓰면 이중 관리되어 어긋난다. 상세는 `java-comment-style` 스킬 §4 참조.

---

## 파일 취급

**여기에는 환경이 바뀌어도 같은 것만 둔다.** 저장 매체·루트 경로·용량·확장자·서빙 방식은
시스템과 배포 환경에 따라 달라지므로 **기능명세서에서 정한다**(`docs/template.md` §5-7).
ILIS는 편집시스템·관리시스템·포털이 별개 시스템이라 그 값들이 서로 다를 수 있다.

- **DB에는 경로만 저장한다.** 파일 자체를 넣지 않는다 (BLOB 미사용 — 기 확정)
- **저장 루트는 설정으로 주입한다.** 코드에 경로를 하드코딩하지 않는다
  ```yaml
  app:
    file:
      root: ${FILE_ROOT}       # 환경별 주입
  ```
- **저장 파일명은 시스템이 생성한다.** 사용자가 올린 이름을 그대로 쓰지 않는다
  (충돌·경로 탈출·인코딩 문제). **원본 파일명은 별도 컬럼에 보관**해 다운로드 시 사용한다
- **경로 조립에 사용자 입력을 그대로 넣지 않는다.** 업무 식별자(사건번호 등)에 `/`·공백·`..`가
  들어갈 수 있다. 숫자 ID나 생성값만 경로에 쓴다
- **파일시스템은 트랜잭션에 참여하지 않는다.** DB는 롤백되어도 쓴 파일은 남는다.
  **어느 쪽을 먼저 할지(파일 선기록 / DB 선기록)는 저장 매체에 따라 유불리가 갈리므로
  기능명세서에서 정한다**(`docs/template.md` §5-7). 규약이 정하는 것은 **보상 처리를 반드시 둔다**는 것이다.

- **보상은 트랜잭션 종료 후에 건다.** `@Transactional` 메서드 안의 `try/catch`는
  **커밋 시점 실패를 잡지 못한다**(커밋은 메서드가 리턴한 뒤 프록시가 수행). 커넥션 유실·제약 위반·타임아웃으로
  커밋이 실패하면 파일만 남는다. 롤백·커밋 실패를 모두 잡으려면 `TransactionSynchronization.afterCompletion`에서
  `STATUS_COMMITTED`가 아닐 때 삭제한다. (구현 코드는 `spring-boot-patterns` 스킬)

- **대용량 업로드는 트랜잭션 밖에서 쓴다.** 트랜잭션 안에서 파일 IO 를 하면 그 시간만큼
  DB 커넥션을 점유해 풀이 고갈된다. 파일을 먼저 쓰고, DB 작업만 트랜잭션으로 감싼다.

- **보상만으로는 완전하지 않다.** 프로세스가 강제 종료되면 보상 로직도 실행되지 않는다.
  **고아 파일 정리 배치가 필요하다** — 운영 여부는 기능명세서에서 정한다.

---

## Java 파일 구조

작성 순서를 통일한다: ① `package` → ② `import`(와일드카드 금지) → ③ 클래스 javadoc(`@author`·`@since`·`@version`) → ④ 애노테이션 → ⑤ 클래스 선언 → ⑥ 상수 → ⑦ `final` 필드(주입) → ⑧ 메소드 javadoc → ⑨ public 메소드 → ⑩ private 헬퍼. (클래스 구조 예시는 `spring-boot-patterns` 스킬의 `LawService`)

---

## 금지 사항

- `import` 와일드카드(`*`) 사용 금지
- Service 인터페이스 생성 금지 (복수 구현 시 예외)
- 트랜잭션 전파 5종 사용 금지 (위 §계층별 규약 참조)
- Controller에서 Mapper 직접 호출 금지
- MyBatis `${}` 무검증 사용 금지

---

## 관련 스킬

- `spring-boot-patterns` — 계층별 코드 작성 패턴
- `java-comment-style` — javadoc·주석 표준
- `java-code-review` — 코드 리뷰
- `db-convention` — DB 명명·컬럼순서·타입·Flyway·DDL·SQL 규약
