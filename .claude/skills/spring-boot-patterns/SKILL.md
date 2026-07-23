---
name: spring-boot-patterns
description: Spring Boot 3 + MyBatis 백엔드의 모범 패턴. 컨트롤러·서비스·MyBatis Mapper·DTO·예외처리·설정·테스트를 만들거나 리뷰할 때 사용. 사용자가 "컨트롤러 만들어", "서비스 추가", "Mapper 작성", "Spring Boot 구조", "REST API", "예외 처리 패턴" 등을 요청할 때 적용. (데이터 접근은 JPA가 아니라 MyBatis 기준.)
---

# Spring Boot Patterns Skill (MyBatis)

Spring Boot 3 애플리케이션의 모범 패턴. **데이터 접근은 MyBatis(Mapper 인터페이스 + XML SQL) 기준**이다.

> 스택: Spring Boot 3.x · JDK 17 이상 · MyBatis 단독(JPA/Hibernate 미사용) · REST · PostgreSQL.
> DBMS는 PostgreSQL 을 작업 전제로 한다 (정식 확정은 출장 담당자 협의).
> 원본(ai-pv) 스킬의 JPA Repository 패턴을 MyBatis Mapper 패턴으로 교체한 발췌본.
> 「Backend 개발가이드 v0.51」 규약을 반영해 갱신(2026-07-20) — 상세는 `backend/CLAUDE.md`.

## When to Use
- "컨트롤러 만들어" / "서비스 추가" / "Mapper 작성" / "Spring Boot 도와줘"
- Spring Boot 코드 리뷰
- 신규 Spring Boot 프로젝트 구조 세팅

---

## ⚠️ 네이밍 규약 — "Mapper" 용어 충돌 주의

MyBatis와 MapStruct **둘 다 `Mapper`를 쓴다.** 혼동을 막기 위해 본 프로젝트는 다음으로 구분한다:

| 역할 | 명명 | 애노테이션 |
|---|---|---|
| **데이터 접근**(SQL) | `XxxMapper` | `@org.apache.ibatis.annotations.Mapper` |
| **엔티티 ↔ DTO 변환**(선택) | `XxxConverter` | `@org.mapstruct.Mapper(componentModel="spring")` |

본 문서에서 `Mapper` = MyBatis 데이터 접근, `Converter` = MapStruct 변환을 뜻한다.

---

## Project Structure

최상위를 **`global`(공통 기반)과 `domain`(업무)으로 2분할**한다. 기술 계층별 평면 분할(controller/service/mapper를 최상위에 나열)을 쓰지 않는다 — 도메인이 늘면 한 패키지에 파일이 몰린다.

```
src/main/java/{base}/               # {base} = 소유 도메인 역순 패키지 (backend/CLAUDE.md)
├── IlisApplication.java           # @SpringBootApplication + @MapperScan  (Ilis = 솔루션명 예시)
├── global/                        # 공통 기반
│   ├── config/                    # [기능]Config
│   │   ├── SecurityConfig.java
│   │   └── WebConfig.java
│   ├── exception/                 # [예외상황]Exception + 전역 핸들러
│   │   ├── ResourceNotFoundException.java
│   │   ├── BusinessException.java
│   │   └── GlobalExceptionHandler.java
│   ├── response/                  # 공통 응답 포맷
│   │   ├── ApiResponse.java
│   │   └── ErrorDetail.java
│   ├── util/                      # Ilis[기능]Utils
│   │   └── IlisDateUtils.java
│   ├── security/jwt/
│   ├── domain/                    # [기능]Enum, [기능]Const, 공통 Dto
│   ├── aop/                       # [기능]Handler
│   └── interceptor/
└── domain/                        # 업무 도메인 단위 (law, precedent, review, assign ...)
    #  주의: Java 예약어(case, new, class ...)는 패키지명 불가 — 판례는 precedent 사용
    └── law/
        ├── api/                   # LawController
        ├── service/               # LawService  (인터페이스 없음 — 단일 클래스)
        ├── repository/            # LawMapper (@Mapper)
        ├── domain/                # Law (POJO — JPA 애노테이션 없음)
        └── dto/                   # LawCreateRequest, LawResponse

src/main/resources/
├── mapper/                        # MyBatis XML (실제 SQL)
│   └── LawMapper.xml
├── application.yml
└── db/migration/                  # 스키마 마이그레이션 (Flyway 등)
```

> base 패키지 `{base}`는 프로젝트가 소유한 도메인의 역순으로 정한다(고정 표준 아님, `backend/CLAUDE.md` §패키지 구조). 예: 정부 시스템이면 `id.go.peraturan.ilis`.

---

## Controller Patterns

### REST Controller Template

모든 응답은 `ApiResponse<T>` 공통 응답 포맷으로 감싼다(§API 공통 응답 포맷 참조).

```java
@RestController
@RequestMapping("/api/v1/laws")
@RequiredArgsConstructor  // Lombok 생성자 주입
public class LawController {

    private final LawService lawService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<LawResponse>>> findLaws(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.success(lawService.findLawsByStatus(status)));
    }

    @GetMapping("/{lawId}")
    public ResponseEntity<ApiResponse<LawResponse>> getLawById(@PathVariable Long lawId) {
        return ResponseEntity.ok(ApiResponse.success(lawService.getLawById(lawId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<LawResponse>> createLaw(
            @Valid @RequestBody LawCreateRequest request) {
        LawResponse created = lawService.createLaw(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{lawId}")
            .buildAndExpand(created.lawId())
            .toUri();
        return ResponseEntity.created(location).body(ApiResponse.success(created));
    }

    @PutMapping("/{lawId}")
    public ResponseEntity<ApiResponse<LawResponse>> updateLawById(
            @PathVariable Long lawId,
            @Valid @RequestBody LawUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(lawService.updateLawById(lawId, request)));
    }

    @DeleteMapping("/{lawId}")
    public ResponseEntity<ApiResponse<Void>> deleteLawById(@PathVariable Long lawId) {
        lawService.deleteLawById(lawId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
```

### Controller Best Practices

| Practice | Example |
|----------|---------|
| Versioned API | `/api/v1/laws` |
| Plural nouns | `/laws` (not `/law`) |
| kebab-case 경로 | `/api/v1/law-articles/{lawId}` |
| HTTP methods | GET=read, POST=create, PUT=update, DELETE=delete |
| 메소드 명명 | `get~By`=단건 / `find~By`=목록 (§Naming 참조) |
| 공통 응답 포맷 | 성공·실패 모두 `ApiResponse<T>` |
| Validation | `@Valid` on request body |

### ❌ Anti-patterns
```java
// ❌ 컨트롤러에 비즈니스 로직 + 데이터 접근 직접 호출
@PostMapping
public LawResponse createLaw(@RequestBody LawCreateRequest request) {
    Law law = new Law(request.title(), request.lawNo());
    lawMapper.insert(law);            // 컨트롤러가 Mapper 직접 호출 — 서비스로
    return new LawResponse(...);
}

// ❌ 도메인/엔티티를 그대로 응답 (내부 구조 노출 + 공통 응답 포맷 누락)
@GetMapping("/{lawId}")
public Law getLawById(@PathVariable Long lawId) {
    return lawMapper.getLawById(lawId);   // DTO + ApiResponse 로 감쌀 것
}

// ❌ 단건 조회에 find 접두사 (get 이어야 함)
public LawResponse findLawById(Long lawId) { ... }
```

---

## Service Patterns

### Service 클래스 (인터페이스 없음)

**서비스 인터페이스를 만들지 않는다.** 하나의 서비스에 여러 구현체가 필요한 경우만 예외.
Mockito는 클래스도 목킹하므로 구현체 하나짜리 인터페이스는 파일만 늘린다.

> 클래스·메소드 javadoc은 **필수**다(`@author`·`@since`·`@version` / `@param`·`@return` + `호출처:`·`흐름:`).
> 아래 예제는 구조에 집중하기 위해 javadoc을 생략했다 — **새 클래스를 만든 직후 `java-comment-style` 스킬을 반드시 적용한다.**

```java
/**
 * 법령 서비스 — 법령 등록·조회·개시 업무 로직 진입점.
 *
 * <p>주요 기능: 법령 단건·목록 조회 / 등록 / 개시(검수 완료 건만)
 *
 * @author 홍길동
 * @since 2026-07-20
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // 클래스 기본 = 읽기 전용
public class LawService {

    private final LawMapper lawMapper;          // MyBatis 데이터 접근
    private final LawConverter lawConverter;    // MapStruct 변환 (선택)

    // 목록 조회 = find 접두사
    public List<LawResponse> findLawsByStatus(String status) {
        return lawConverter.toResponseList(lawMapper.findLawsByStatus(status));
    }

    // 단건 조회 = get 접두사
    public LawResponse getLawById(Long lawId) {
        Law law = lawMapper.getLawById(lawId);   // 없으면 null
        if (law == null) {
            throw new ResourceNotFoundException("Law", lawId);
        }
        return lawConverter.toResponse(law);
    }

    @Transactional  // 쓰기 트랜잭션
    public LawResponse createLaw(LawCreateRequest request) {
        Law law = lawConverter.toEntity(request);
        lawMapper.insertLaw(law);                // useGeneratedKeys 로 law.lawId 채워짐
        return lawConverter.toResponse(law);
    }

    @Transactional
    public LawResponse updateLawById(Long lawId, LawUpdateRequest request) {
        Law law = lawMapper.getLawById(lawId);
        if (law == null) {
            throw new ResourceNotFoundException("Law", lawId);
        }
        law.update(request.title(), request.lawNo());  // 도메인 메서드로 변경 반영
        lawMapper.updateLawById(law);
        return lawConverter.toResponse(law);
    }

    @Transactional
    public void deleteLawById(Long lawId) {
        int affected = lawMapper.deleteLawById(lawId);
        if (affected == 0) {                     // 삭제 대상 없음 = 404
            throw new ResourceNotFoundException("Law", lawId);
        }
    }
}
```

### Service Best Practices

- **인터페이스를 만들지 않는다** (복수 구현이 실제로 필요할 때만 예외)
- 클래스에 `@Transactional(readOnly = true)`, 쓰기 메서드에만 `@Transactional`
- **트랜잭션 전파는 `REQUIRED`(기본) · `REQUIRES_NEW` 만 사용**한다.
  `SUPPORTS` `NOT_SUPPORTED` `MANDATORY` `NEVER` `NESTED` 는 사용 금지.
- 일반 예외가 아니라 도메인 예외를 던진다
- **Mapper는 null / 영향 행 수를 반환** — 서비스가 null 검사·행 수 검증으로 404/충돌 판단
- 엔티티 ↔ DTO 변환은 MapStruct(`Converter`) 또는 명시적 팩토리 메서드로 (컨트롤러/Mapper에 흩지 않음)

### 트랜잭션 전파

```java
@Transactional                                            // REQUIRED (기본)
public void publishLaw(Long lawId) { ... }

@Transactional(propagation = Propagation.REQUIRES_NEW)    // 부모 롤백에 영향받지 않음
public void saveAuditLog(String message) { ... }          // 로그·감사 기록 등
```

| 속성 | 사용 | 설명 |
|---|---|---|
| `REQUIRED` (기본값) | ✅ | 부모 트랜잭션이 있으면 합류, 없으면 새로 시작 |
| `REQUIRES_NEW` | ✅ | 항상 새 트랜잭션 — 로그 저장 등 별도 처리 |
| `SUPPORTS` `NOT_SUPPORTED` `MANDATORY` `NEVER` `NESTED` | ❌ | 사용 금지 |

---

## MyBatis Mapper Patterns

### Mapper 인터페이스
```java
@Mapper  // org.apache.ibatis.annotations.Mapper
public interface LawMapper {

    // 단건 = get 접두사 — 없으면 null (서비스에서 null 검사)
    Law getLawById(@Param("lawId") Long lawId);

    // 목록 = find 접두사
    List<Law> findLawsByStatus(@Param("status") String status);

    // 존재 확인 — get/find 보다 가벼움
    boolean isLawNoExists(@Param("lawNo") String lawNo);

    int insertLaw(Law law);             // 반환 = 영향 행 수, keyProperty 로 PK 채움
    int updateLawById(Law law);
    int deleteLawById(@Param("lawId") Long lawId);

    // 다중 파라미터는 @Param 필수
    List<Law> findLawsByKeyword(@Param("keyword") String keyword,
                                @Param("limit") int limit,
                                @Param("offset") int offset);
}
```

> **접두사 규칙이 Mapper에도 적용된다** — `get`=단건, `find`=목록.
> Spring Data 관례(`findById`가 단건)와 다르지만 MyBatis 단독이므로 충돌하지 않는다.

### Mapper XML (실제 SQL)

> SQL은 **PostgreSQL 문법** 기준이다 (작업 전제).

```xml
<!-- src/main/resources/mapper/LawMapper.xml -->
<mapper namespace="{base}.domain.law.repository.LawMapper">

  <resultMap id="lawMap" type="{base}.domain.law.domain.Law">
    <id     property="lawId"     column="law_id"/>
    <result property="lawNo"     column="law_no"/>
    <result property="title"     column="title"/>
    <result property="status"    column="status"/>
    <result property="createdAt" column="created_at"/>
  </resultMap>

  <sql id="lawCols">law_id, law_no, title, status, created_at</sql>

  <select id="getLawById" resultMap="lawMap">
    SELECT <include refid="lawCols"/> FROM law WHERE law_id = #{lawId}
  </select>

  <select id="findLawsByStatus" resultMap="lawMap">
    SELECT <include refid="lawCols"/> FROM law
    <where>
      <if test="status != null and status != ''">
        AND status = #{status}
      </if>
    </where>
    ORDER BY created_at DESC
  </select>

  <select id="isLawNoExists" resultType="boolean">
    SELECT EXISTS(SELECT 1 FROM law WHERE law_no = #{lawNo})
  </select>

  <insert id="insertLaw" useGeneratedKeys="true" keyProperty="lawId">
    INSERT INTO law (law_no, title, status, created_at)
    VALUES (#{lawNo}, #{title}, #{status}, #{createdAt})
  </insert>

  <update id="updateLawById">
    UPDATE law SET law_no = #{lawNo}, title = #{title}
    WHERE law_id = #{lawId}
  </update>

  <delete id="deleteLawById">
    DELETE FROM law WHERE law_id = #{lawId}
  </delete>

  <!-- 동적 SQL: 조건은 Java가 아니라 XML에서 조립 -->
  <select id="findLawsByKeyword" resultMap="lawMap">
    SELECT <include refid="lawCols"/>
    FROM law
    <where>
      <if test="keyword != null and keyword != ''">
        AND title LIKE '%' || #{keyword} || '%'
      </if>
    </where>
    ORDER BY created_at DESC
    LIMIT #{limit} OFFSET #{offset}
  </select>
</mapper>
```

### MyBatis Best Practices

- **`#{}` = 바인딩 파라미터**(PreparedStatement, SQL 인젝션 방어). **`${}` = 문자열 치환**이라 화이트리스트 검증을 통과한 값(컬럼명·정렬 방향)에만 사용.
- 다중 파라미터 메서드는 `@Param` 명시 (없으면 `param1`, `arg0` 로 접근해야 함).
- snake_case ↔ camelCase 는 `map-underscore-to-camel-case: true` 또는 `resultMap`으로.
- **동적 조건(WHERE 분기·정렬·IN 목록)은 XML의 `<if>`/`<choose>`/`<foreach>`/`<where>`** 로. Java 문자열 조립 금지.
- 단건 조회는 없으면 `null` — 서비스에서 `Optional` 감싸거나 예외로 처리.
- `insert`/`update`/`delete`는 **영향 행 수(int)** 반환 → 서비스에서 0건 검증.
- N+1 회피: 연관 로딩은 `<association>`/`<collection>` 중첩 resultMap 또는 JOIN 한 방으로.
- SQL 자체 설명(조인·분기·정렬)은 XML 주석에 둔다 — Java Mapper javadoc은 역할·반환 형태만.

---

## DTO Patterns

### Request/Response DTOs
```java
// 검증 애노테이션 붙은 요청 DTO
public record CreateUserRequest(
    @NotBlank(message = "이름은 필수")
    @Size(min = 2, max = 100)
    String name,

    @NotBlank
    @Email(message = "이메일 형식 오류")
    String email,

    @NotNull
    @Min(18)
    Integer age
) {}

// 수정 요청 DTO (부분 수정 대상 필드만)
public record UpdateUserRequest(
    @NotBlank @Size(min = 2, max = 100)
    String name,

    @NotBlank @Email
    String email
) {}

// 응답 DTO
public record UserResponse(
    Long id,
    String name,
    String email,
    LocalDateTime createdAt
) {}
```

### MapStruct Converter (선택)
```java
@Mapper(componentModel = "spring")  // org.mapstruct.Mapper — MyBatis @Mapper 와 다름!
public interface UserConverter {

    UserResponse toResponse(User entity);

    List<UserResponse> toResponseList(List<User> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User toEntity(CreateUserRequest request);
}
```
> MapStruct를 안 쓰면 이 변환을 명시적 정적 팩토리(`UserResponse.from(user)`)로 대체해도 된다.

---

## Exception Handling

### Custom Exceptions
```java
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Long id) {
        super(String.format("%s not found with id: %d", resource, id));
    }
}

/**
 * 업무 규칙에 의한 거절 — 요청 자체는 정상 처리된 것이므로 HTTP 200 + status=FAIL 로 응답된다.
 * (상태 전이 불가·중복 등록·기한 경과 등. 권한은 해당하지 않는다 — Security 계층에서 403)
 *
 * <p><b>문구를 담지 않는다.</b> code 만 들고 다니고, 사용자에게 보일 문구는
 * 전역 핸들러가 {@code MessageSource} 에서 요청 언어에 맞춰 조회한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String code;
    private final Object[] args;      // 메시지에 끼워 넣을 값 (선택)

    public BusinessException(String code, Object... args) {
        super(code);                  // 로그용 — 사용자에게 나가지 않는다
        this.code = code;
        this.args = args;
    }
}
```

### Global Exception Handler
```java
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    // 대상 부재 — 자원 부재 관례에 따라 404 (업무 거절 200 이 아님)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, Locale locale) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.fail("NOT_FOUND", messageSource.getMessage("error.NOT_FOUND", null, locale)));
    }

    // 업무 규칙 거절 — 요청은 정상 처리됨 → HTTP 200
    // 문구는 code 로 MessageSource 에서 조회 (Accept-Language 반영)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, Locale locale) {
        log.info("Business rule rejected: {}", ex.getCode());
        String message = messageSource.getMessage("error." + ex.getCode(), ex.getArgs(), locale);
        return ResponseEntity.ok(ApiResponse.fail(ex.getCode(), message));
    }

    // 요청 형식 오류 — 업무 로직 도달 못 함 → HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException ex, Locale locale) {
        // Bean Validation 메시지는 {validation.xxx} 키 참조 → MessageSource 가 이미 번역해 넣는다
        List<ErrorDetail> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> new ErrorDetail(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
            .toList();
        return ResponseEntity.badRequest()
            .body(ApiResponse.fail("INVALID_INPUT",
                messageSource.getMessage("error.INVALID_INPUT", null, locale), errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneric(Exception ex, Locale locale) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail("SERVER_ERROR",
                messageSource.getMessage("error.SERVER_ERROR", null, locale)));
    }
}
```

---

## API 공통 응답 포맷

**모든 응답을 공통 응답 포맷으로 감싼다.** 공통 응답 포맷의 `status`/`code`는 업무 결과를, HTTP 상태 코드는 요청이 처리되었는가를 나타낸다. 둘은 별개이며 일치하지 않을 수 있다.

### HTTP 상태 코드 판단 기준

기준은 **요청 자체가 유효한가**와 **업무 규칙의 판정 결과가 무엇인가**를 나누는 것이다.
요청의 유효성은 네 가지로 본다 — **주소·형식·인증·대상 존재 여부**. 넷을 통과한 뒤 업무 규칙이 거절한 것만 200이다.

| 상황 | HTTP | `status` | 예시 |
|---|---|---|---|
| 정상 처리·정상 결과 | 200 / 201 | `SUCCESS` | 조회 성공, 등록 완료 |
| 요청은 유효, 업무 규칙이 거절 | **200** | `FAIL` | 아래 닫힌 목록 참조 |
| 요청 형식·값이 잘못됨 | 400 | `FAIL` | 필수값 누락, 타입 불일치 |
| 인증 실패 | 401 | `FAIL` | 토큰 없음·만료 |
| **인가 실패 (권한 전반)** | 403 | `FAIL` | Security 계층 차단 — 역할·데이터 기준 모두 |
| 대상이 존재하지 않음 | 404 | `FAIL` | 없는 경로, **없는 법령 ID** |
| 서버 오류 | 500 | `FAIL` | 예기치 못한 예외 |

**404는 자원 부재 관례를 따른다** — 없는 경로든 없는 법령 ID든 둘 다 404이며 업무 거절(200)이 아니다.

**200 + FAIL 로 내리는 경우 (닫힌 목록)** — 업무 규칙 거절만 해당한다.

- 상태 전이 불가 — 검수 미완료 건의 개시 시도, 이미 개시된 건의 수정 시도
- 중복 등록 — 이미 존재하는 법령 번호·사건번호
- 기한 경과 — 마감된 작업에 대한 요청

**권한은 이 목록에 없다.** 역할 기반이든 데이터 기반이든 권한 체크는 Service 안에서 하지 않고
Security 계층(`@PreAuthorize` + 필요 시 `PermissionEvaluator`)에서 차단해 **403**을 내린다.
Service 본문에 권한 분기를 넣으면 업무 로직과 인가 로직이 섞이고 같은 판정이 여러 메소드에 흩어진다.

```java
// 역할 기준
@PreAuthorize("hasRole('REVIEWER')")
public LawResponse completeReviewById(Long lawId) { ... }

// 데이터 기준 — 본인 배정 문서인지 판정도 메서드 시큐리티로 끌어올린다
@PreAuthorize("@lawPermission.canEdit(#lawId)")
public LawResponse updateLawById(Long lawId, LawUpdateRequest request) { ... }
```

> **트레이드오프**: 업무 거절이 200이므로 HTTP 상태 코드만으로는 업무 실패율을 볼 수 없다.
> 공통 응답 포맷의 `code` 기준 메트릭을 별도 수집해야 한다.

### ApiResponse

```java
public record ApiResponse<T>(
    String status,              // SUCCESS | FAIL
    String code,                // OK | INVALID_INPUT | NOT_FOUND | SERVER_ERROR ...
    String message,
    T data,
    List<ErrorDetail> errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "OK", "요청이 성공했습니다.", data, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>("FAIL", code, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String code, String message, List<ErrorDetail> errors) {
        return new ApiResponse<>("FAIL", code, message, null, errors);
    }
}

public record ErrorDetail(String field, Object rejectedValue, String message) {}
```

> 공통 응답 포맷 조립은 `ApiResponse` 정적 팩토리로만 한다 — Controller마다 직접 `new` 하지 않는다.
> 예외 → 공통 응답 포맷 변환은 `@RestControllerAdvice` 한 곳에 모은다.

### 응답 예시

```json
// 성공
{ "status": "SUCCESS", "code": "OK", "message": "요청이 성공했습니다.", "data": { "lawId": 1 } }

// 업무 거절 (HTTP 200)
{ "status": "FAIL", "code": "NOT_REVIEWED", "message": "검수가 완료되지 않아 개시할 수 없습니다." }

// 검증 실패 (HTTP 400)
{
  "status": "FAIL",
  "code": "INVALID_INPUT",
  "message": "입력값이 올바르지 않습니다.",
  "errors": [
    { "field": "lawNo", "rejectedValue": "", "message": "법령 번호는 필수입니다." }
  ]
}
```

### 목록 응답 — PageResponse

목록은 배열을 그대로 내리지 않고 `PageResponse<T>`로 감싼다 → `ApiResponse<PageResponse<T>>`. (규칙 상세는 `backend/CLAUDE.md`)

```java
public record PageResponse<T>(
    List<T> content,        // 현재 페이지 항목
    int page,               // 0-based 페이지 번호
    int size,               // 페이지 크기
    long totalElements,     // 전체 건수
    int totalPages          // 전체 페이지 수
) {}
```

```json
{ "status": "SUCCESS", "code": "OK", "message": "요청이 성공했습니다.",
  "data": { "content": [ ... ], "page": 0, "size": 20, "totalElements": 137, "totalPages": 7 } }
```

> 페이징 없는 짧은 목록은 배열 허용. 같은 엔드포인트가 배열과 `PageResponse`를 번갈아 내리지 않는다.

---

## Configuration Patterns

### Application Properties
```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL}                                # 환경별 주입 — 코드에 박지 않는다
    username: ${DB_USER}
    password: ${DB_PASSWORD}

mybatis:
  mapper-locations: classpath:mapper/**/*.xml   # XML 위치
  type-aliases-package: {base}                  # {base} = 프로젝트 base 패키지 (backend/CLAUDE.md, 고정 표준 아님)
  configuration:
    map-underscore-to-camel-case: true          # snake_case → camelCase
    jdbc-type-for-null: NULL

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration: 86400000
```
> 스키마는 MyBatis가 만들지 않는다(JPA `ddl-auto` 개념 없음). Flyway 등 마이그레이션 도구로 관리.

### Configuration Properties Class
```java
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public class JwtProperties {

    @NotBlank
    private String secret;

    @Min(60000)
    private long expiration;

    // getters and setters
}
```

### Profile-Specific Configuration
```
src/main/resources/
├── application.yml           # 공통
├── application-dev.yml       # 개발
├── application-test.yml      # 테스트
└── application-prod.yml      # 운영
```

### 다국어 — Accept-Language / MessageSource

프론트가 모든 요청에 `Accept-Language`(`id`|`en`|`ko`)를 보내면, 백엔드는 그 언어 문구를 `message`·`errors[].message`에 담는다. 예외는 `code`만 들고, 문구는 전역 핸들러가 `MessageSource`에서 조회한다(위 `GlobalExceptionHandler` 참조). (규칙 상세는 `backend/CLAUDE.md`)

```
src/main/resources/messages/
├── messages.properties        # 기본 (id — 인도네시아어)
├── messages_en.properties
└── messages_ko.properties
```

```properties
# messages.properties
error.NOT_REVIEWED=Belum diverifikasi sehingga tidak dapat diterbitkan.
validation.lawNo.required=Nomor peraturan wajib diisi.
```

```java
@Configuration
public class LocaleConfig {
    @Bean
    public LocaleResolver localeResolver() {                    // Accept-Language 헤더로 언어 결정
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setSupportedLocales(List.of(Locale.forLanguageTag("id"), Locale.ENGLISH, Locale.KOREAN));
        resolver.setDefaultLocale(Locale.forLanguageTag("id"));  // Locale.of 는 Java 19+ — JDK 17 안전
        return resolver;
    }
}
```
> 메시지 키가 없으면 예외가 나도록 둔다(조용한 fallback 금지). 3개 언어 리소스를 동시에 채운다.

---

## Common Annotations Quick Reference

| Annotation | Purpose |
|------------|---------|
| `@RestController` | REST 컨트롤러 (@Controller + @ResponseBody) |
| `@Service` | 비즈니스 로직 컴포넌트 |
| `@Mapper` (MyBatis) | 데이터 접근 인터페이스 — XML/애노테이션 SQL 바인딩 |
| `@MapperScan` | Mapper 패키지 일괄 스캔 (설정 클래스에) |
| `@Configuration` | 설정 클래스 |
| `@RequiredArgsConstructor` | Lombok 생성자 주입 |
| `@Transactional` | 트랜잭션 관리 |
| `@Valid` | 검증 트리거 |
| `@ConfigurationProperties` | 프로퍼티 바인딩 |
| `@Profile("dev")` | 프로파일별 빈 |
| `@Scheduled` | 스케줄 작업 |

---

## Testing Patterns

### Controller Test (MockMvc)

공통 응답 포맷으로 감싸므로 `jsonPath`는 `$.data.` 아래를 본다.

```java
@WebMvcTest(LawController.class)
class LawControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LawService lawService;

    @Test
    void shouldReturnLaw() throws Exception {
        when(lawService.getLawById(1L))
            .thenReturn(new LawResponse(1L, "UU 1/2022", "HKPD", null));

        mockMvc.perform(get("/api/v1/laws/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("SUCCESS"))
            .andExpect(jsonPath("$.data.lawNo").value("UU 1/2022"));
    }

    // 업무 규칙 거절은 상태를 바꾸는 요청에서 발생한다 — 단순 조회는 대상 유무(404)만 갈린다
    @Test
    void shouldReturn200WithFailWhenNotReviewed() throws Exception {
        when(lawService.publishLawById(1L))
            .thenThrow(new BusinessException("NOT_REVIEWED"));

        mockMvc.perform(post("/api/v1/laws/1/publish"))
            .andExpect(status().isOk())            // 업무 규칙 거절 = HTTP 200
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.code").value("NOT_REVIEWED"));
    }

    @Test
    void shouldReturn404WhenLawNotFound() throws Exception {
        when(lawService.getLawById(99L))
            .thenThrow(new ResourceNotFoundException("Law", 99L));

        mockMvc.perform(get("/api/v1/laws/99"))
            .andExpect(status().isNotFound())      // 대상 부재 = HTTP 404 (업무 거절 아님)
            .andExpect(jsonPath("$.status").value("FAIL"))
            .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }
}
```

### Service Test

서비스 인터페이스가 없으므로 구현 클래스를 그대로 목킹·주입한다.

```java
@ExtendWith(MockitoExtension.class)
class LawServiceTest {

    @Mock
    private LawMapper lawMapper;          // MyBatis Mapper mock

    @Mock
    private LawConverter lawConverter;

    @InjectMocks
    private LawService lawService;        // 인터페이스 없음 — 클래스 직접 주입

    @Test
    void shouldThrowWhenLawNotFound() {
        when(lawMapper.getLawById(1L)).thenReturn(null);   // 없으면 null

        assertThatThrownBy(() -> lawService.getLawById(1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
```

### Integration Test (Testcontainers)
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserIntegrationTest {

    @Container
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:17");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUser() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "John", "email": "john@example.com", "age": 25}
                    """))
            .andExpect(status().isCreated());
    }
}
```
> MyBatis Mapper만 슬라이스 테스트하려면 `@MybatisTest`(mybatis-spring-boot-starter-test) + Testcontainers 조합.

---

## 파일 취급 — 트랜잭션 보상

파일시스템은 트랜잭션에 참여하지 않는다(DB가 롤백돼도 쓴 파일은 남음). 보상 삭제는 **트랜잭션 종료 후** `afterCompletion`으로 건다 — `@Transactional` 안의 `try/catch`는 커밋 시점 실패를 못 잡는다(커밋은 메서드 리턴 후 프록시가 수행). (원칙·규칙은 `backend/CLAUDE.md` §파일 취급)

```java
@Transactional
public void addAttachment(...) {
    String storedPath = fileStore.write(...);
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {   // 롤백·커밋 실패 모두 포함
                    fileStore.delete(storedPath);
                }
            }
        });
    attachmentMapper.insertAttachment(...);
}
```
> 대용량 업로드는 트랜잭션 밖에서 파일 IO(커넥션 점유 방지). 프로세스 강제 종료 대비 고아 파일 정리 배치 필요(운영 여부는 기능명세서).

---

## Quick Reference Card

| Layer | Responsibility | Annotations |
|-------|---------------|-------------|
| Controller | HTTP 처리, 검증, 공통 응답 포맷 반환 | `@RestController`, `@Valid` |
| Service | 비즈니스 로직, 트랜잭션 (**인터페이스 없음**) | `@Service`, `@Transactional` |
| Mapper (MyBatis) | 데이터 접근 (SQL은 XML) | `@Mapper` |
| Converter (MapStruct) | 엔티티 ↔ DTO 변환 (선택) | `@Mapper(componentModel="spring")` |
| DTO | 데이터 전송 | 검증 애노테이션 붙은 record |
| Config | 설정 | `@Configuration`, `@ConfigurationProperties` |
| Exception | 에러 처리 → 공통 응답 포맷 변환 | `@RestControllerAdvice` |

### 규약 요약 (프로젝트 결정 사항)

| 항목 | 규약 |
|---|---|
| 패키지 | `global` / `domain` 2분할 — 계층 평면 분할 금지 |
| Service 인터페이스 | 만들지 않음 (복수 구현 시만 예외) |
| 조회 메소드 | `get~By`=단건 / `find~By`=목록(조건1) / `findAll~`=조건없음 / `find~`=복합·선택 조건 |
| 응답 | 전부 `ApiResponse<T>` 공통 응답 포맷 |
| HTTP 상태 | 업무 거절=200, 요청 불가=4xx, 장애=5xx |
| 트랜잭션 전파 | `REQUIRED` · `REQUIRES_NEW` 만 |
| import | 와일드카드(`*`) 금지 |

> 상세는 `backend/CLAUDE.md` 참조. 근거는 「Backend 개발가이드 v0.51」.
