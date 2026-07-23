---
name: java-comment-style
description: Java 백엔드 클래스(Service/Controller/Mapper/IT)의 javadoc·인라인 주석을 일관 표준으로 작성한다. 사용자가 "주석 추가", "주석 표준 적용", "javadoc 작성", "주석 일관성 정리", "comment style", "주석 보강" 등을 요청하거나, 새 클래스를 생성·리팩토링한 직후 자동 적용한다. 호출 그래프(`호출처:` / `사용처:`)와 반환 자료구조 예시를 명시하고, PR 시점 컨텍스트(`#issue`/`Phase N`/`결함 N`/날짜)는 제거한다.
---

# Java 주석 표준 스킬

백엔드 Java 클래스의 javadoc·인라인 주석을 일관된 톤으로 작성·정리한다. **다른 개발자가 코드를 처음 봤을 때 호출 그래프와 동작 결과를 즉시 파악할 수 있도록** 하는 게 목표.

> 적용 범위: Java 클래스(Service / Controller / Mapper 인터페이스 / IT 테스트)의 javadoc·인라인 주석.
> **MyBatis XML 매퍼의 SQL 주석은 본 스킬 대상이 아니다** — XML은 javadoc이 없으므로 별도(MyBatis) 표준을 따른다.

## When to Use

- "이 클래스에 주석 추가해줘" / "javadoc 작성해줘"
- "주석 일관성 정리해줘" / "주석 보강"
- "주석 표준대로 다시 써줘"
- 신규 Service/Controller/Mapper 클래스 생성 직후 (자동 적용)
- 큰 리팩토링 후 (메서드 시그니처/책임 변경 후)
- IT 테스트 클래스의 시나리오 주석 정리

**적용 안 함**: DTO record (필드명이 자기 설명적), 단순 setter/getter, MyBatis XML 매퍼(별도 표준).

---

## 원칙 4개

| 원칙 | 의미 |
|---|---|
| **사실 기반** | "왜 이 동작이 필요한가"만 — 코드가 표현하지 못하는 *제약·의도·부작용* |
| **계약 명시** | 클래스는 `@author`·`@since`·`@version`, 메서드는 `@param`·`@return` (필수 — 사내 가이드) |
| **호출 그래프** | 어디서 호출되는가 (`호출처:`) + 어느 헬퍼·상수를 쓰는가 (`사용처:` / `내부 호출:`) |
| **결과 예시** | 메서드가 반환하는 자료구조·값의 형태 (필요 시 ASCII 예시) |
| **외부 참조 금지** | `#16-B` / `Phase 4` / `결함 1` 같은 PR 시점 컨텍스트는 git log·명세서·작업일지에 위임 |

> **본 스킬은 사내 「Backend 개발가이드 v0.51」의 필수 태그와 kit 원본의 호출 그래프를 병합한 판이다**(2026-07-20).
> 가이드가 요구하는 계약 태그를 지키되, kit의 `호출처:`·`흐름:` 섹션을 추가로 유지한다.
> **`@since`의 작성일은 외부 참조 금지 대상이 아니다** — 가이드가 요구하는 필수 태그다.

---

## Quick Reference — 4 카테고리 템플릿

| 카테고리 | 필수 섹션 |
|---|---|
| **클래스 javadoc** | 역할 1줄 / 주요 기능 / (활성 조건) / (위임·의존성) / 책임 범위 / **`@author`·`@since`·`@version`** |
| **public 메서드** | 역할 1줄 / `호출처:` / `흐름:` / **`@param`·`@return`** / (`@throws`) |
| **private 헬퍼** | 의미 1줄 / (예시) / `사용처:` / (`내부 호출:`) |
| **Mapper 인터페이스 메서드** | 역할 1줄 / 반환 형태 / **`@param`·`@return`** / (대응 XML) — `흐름:` 없음 |

---

## 1. 클래스 javadoc 템플릿

```java
/**
 * <역할 1줄 — 무엇을 담당하는 클래스인가>.
 *
 * <p>(해당 시) 활성 조건 / 위치:
 *   {@code @ConditionalOnProperty(...)} 또는 빈 등록 조건
 *
 * <p>(해당 시) 위임 / 의존성:
 *   상위 클래스(extends) 또는 의존 컴포넌트 — 어떤 책임을 위임/공유하는가
 *
 * <p>본 클래스 책임 범위:
 * <ul>
 *   <li>책임 1 — 도구 선택 사유</li>
 *   <li>책임 2</li>
 * </ul>
 *
 * <p>(해당 시) 트랜잭션 / Audit / 보안 가드 — 어느 표준을 따르는가
 *
 * @author <작성자>
 * @since <최초 작성일 YYYY-MM-DD>
 * @version <버전>
 */
```

**예시 (Service)**:

```java
/**
 * 상품기초정보 서비스 — MENU_02_01 화면 백엔드 진입점.
 *
 * <p>역할:
 * <ul>
 *   <li>그리드 메타 조회 / 행 다건·페이징 조회 / 저장(UPSERT) / 삭제 / 엑셀 다운로드·파싱</li>
 *   <li>컬럼 메타 기준으로 TREE_NODE_DRAFT / PRODUCT_DRAFT 두 테이블에 값을 분배</li>
 * </ul>
 *
 * <p>{@link ProductBaseInfoController} 의 모든 엔드포인트가 본 서비스로 위임.
 * 트랜잭션 경계는 본 서비스의 {@code @Transactional} 메서드(저장·삭제)에서만 시작.
 *
 * <p>Audit: {@link #currentActor} + {@link OffsetDateTime#now()} 를 본 서비스에서 생성하여 Mapper에 전달.
 *
 * @author 홍길동
 * @since 2026-07-20
 * @version 1.0
 */
```

---

## 2. public 메서드 javadoc 템플릿

```java
/**
 * <메서드 역할 1줄>.
 *
 * <p>호출처:
 * <ul>
 *   <li>{@code XxxController.method()} — {@code GET /endpoint}</li>
 *   <li>{@link #otherMethod} — (내부 호출인 경우 사유)</li>
 * </ul>
 *
 * <p>흐름:
 * <ol>
 *   <li>단계 1 — {@link #helper1}</li>
 *   <li>단계 2 — {@link Mapper#method}</li>
 * </ol>
 *
 * <p>(해당 시) 트랜잭션 / 가드 / 응답 매핑 — 추가 설명
 *
 * @param <파라미터명> <의미. 단건/목록·null 허용 여부 등 이름만으로 모르는 것>
 * @return <반환 자료구조의 형태 — 단건 / 다건(빈 컬렉션 규약) / 집계값. null 가능 여부 명시>
 * @throws <의미 있는 예외 + 발생 조건> (선택 — 프레임워크 런타임 예외는 생략)
 */
```

> **계약 태그 원칙**: `@param`·`@return`은 **필수**로 단다(사내 가이드). `@throws`는 선택 —
> 의미 있는 도메인 예외만 쓰고 프레임워크 런타임 예외는 생략한다.
> 태그 내용은 이름·타입이 이미 말하는 것을 반복하지 말고 **null·빈 컬렉션 규약, 단건/목록 구분, 값의 범위**처럼
> 코드로 안 보이는 것을 적는다.
>
> ```java
> @param lawId 조회할 법령 ID          ← ✗ 이름이 이미 말함
> @param lawId 개시된 법령만 조회 대상   ← ✓ 코드로 안 보이는 제약
> ```

**Service 예시**:

```java
/**
 * 페이징 + 정렬·필터 검색 — F 폴더 후손 P 노드 + 입력 P 노드 prefix 합쳐 반환.
 *
 * <p>호출처: {@code ProductBaseInfoController.searchRows()} — {@code POST /rows/search?menuId&gridId}.
 *
 * <p>흐름:
 * <ol>
 *   <li>{@link Mapper#findColumnsByGrid} — 메타 조회 후 화이트리스트 빌드</li>
 *   <li>req.sort / filters 화이트리스트 통과만 추출 — 미허용 키 silently drop</li>
 *   <li>nodeType 분기 (P / F)</li>
 *   <li>P prefix: {@link Mapper#findRowsByNodeIds}</li>
 *   <li>F 페이징: {@link Mapper#searchRowsBySubtreeRoots} + {@link Mapper#countRowsBySubtreeRoots}</li>
 * </ol>
 */
```

**Controller 예시**:

```java
/**
 * 상품기초정보 행 검색 — 그리드 페이징·정렬·필터 요청 수신.
 *
 * <p>호출처: {@code POST /api/product-base/rows/search?menuId&gridId} (그리드 프론트).
 *
 * <p>흐름:
 * <ol>
 *   <li>{@code @Valid} 요청 바디 검증 — 페이지 크기 상한 가드</li>
 *   <li>{@link ProductBaseInfoService#searchRows} 로 위임</li>
 *   <li>결과를 {@link PageResponse} DTO로 매핑하여 반환</li>
 * </ol>
 *
 * <p>권한: {@code @PreAuthorize("hasRole('PRODUCT_VIEW')")} — 조회 권한 필요.
 */
```

---

## 3. private 헬퍼 javadoc 템플릿

```java
/**
 * <헬퍼 의미 1줄>. <부가 동작>.
 *
 * <p>예시:           ← 변환·매핑·검증 헬퍼만 (선택)
 *   input  →  output
 *
 * <p>사용처: {@link #메서드1}, {@link #메서드2}.
 *
 * <p>(해당 시) 내부 호출: {@link #다른헬퍼}.
 */
```

**예시**:

```java
/**
 * 요청 정렬 키를 화이트리스트로 필터 — 미허용 키는 무시하고 기본 정렬로 대체.
 * 예시: normalizeSort("dropTable;--")  →  기본 정렬("nodeId ASC")
 *
 * <p>사용처: {@link #searchRows} — 동적 정렬 파라미터 검증 시.
 */
private static String normalizeSort(String rawSort, Set<String> allowed) { ... }
```

---

## 4. Mapper 인터페이스 메서드 (특례)

MyBatis Mapper 인터페이스 메서드는 **본체가 없고 실제 SQL·처리 흐름은 XML에 있다.** 따라서 위 public 메서드 템플릿의 `흐름:`을 쓰지 않는다(흐름을 Java 주석에 쓰면 XML과 이중 관리되어 어긋난다). 다음만 남긴다:

- **역할 1줄** — 무엇을 조회/변경하는가
- **`@param` · `@return`** (필수) — 반환은 단건 / 다건(빈 리스트 규약) / 집계값, null 가능 여부
- **대응 XML** — `MapperId.statementId` (선택 — XML statement를 빨리 찾게)

```java
/**
 * 상태별 법령 다건 조회.
 *
 * <p>대응 XML: {@code LawMapper.findLawsByStatus}.
 *
 * @param status 조회할 상태 코드. null이면 전체 조회
 * @return 매칭된 법령 {@code List} — 없으면 빈 리스트(null 아님)
 */
List<Law> findLawsByStatus(@Param("status") String status);

/**
 * 법령 ID로 단건 조회.
 *
 * <p>대응 XML: {@code LawMapper.getLawById}.
 *
 * @param lawId 조회할 법령 ID
 * @return 법령 단건 — 존재하지 않으면 {@code null} (호출부에서 null 검사 필요)
 */
Law getLawById(@Param("lawId") Long lawId);
```

**SQL 자체에 대한 설명(조인·동적 분기·정렬 조립)은 여기 쓰지 않고 XML 주석에 둔다** — 본 스킬 범위 밖(MyBatis 표준).

---

## 5. 인라인 박스 주석 (`// === //`)

**사용 시점**: 메서드 그룹 구분 (메타 / 행 조회 / 저장 / 헬퍼).
**원칙**: javadoc과 중복 시 박스는 *그룹 표지*만, 자세한 내용은 javadoc.

```java
// ================================================================== //
// 메타 조회 그룹                                                       //
// ================================================================== //
//
// 호출처: ProductBaseInfoController — GET /meta 계열 엔드포인트.
//
// 패턴: 그룹 표지만. 각 메서드의 상세는 해당 메서드 javadoc 참조.
```

---

## 6. 안티패턴 (제거 대상)

```
✗ #16-A / #16-B / #16-C         → 명세서 결정 번호
✗ Phase 4 단순화                → PR 시점 컨텍스트
✗ 사용자 작업 기준              → 변경 컨텍스트
✗ 결함 1 (#16-B) 검증          → 이슈 번호
✗ "added for the Y flow"        → 작업 시점 커밋 메시지화
✗ "기존엔 X였는데 이제 Y"       → 코드 진화 이력 (git log에 위임)
```

**이유**: 명세서/이슈 번호는 코드와 별도로 진화한다. 6개월 뒤 `#16-B`만 남으면 의미 잃어버린다. 사실(*왜 이 동작이 필요한가*)만 남기고 이력은 git log + 명세서 + 작업일지에 위임.

> **날짜는 안티패턴이 아니다** — 사내 가이드가 `@since`(최초 작성일)를 필수로 요구한다.
> 금지 대상은 *변경을 유발한 작업 시점*(`2026-04-30 수정`)이지 *클래스 최초 작성일*이 아니다.
> kit 원본은 날짜를 일괄 금지했으나 본 판에서 해제한다(결정 6).

### 안티패턴 검출 명령

```bash
# 외부 참조 자동 탐지 — @since·@version 의 날짜는 정상이므로 제외
grep -rn "#[0-9]\|Phase [0-9]\|결함 [0-9]\|작업 기준" \
  backend/src/main/java/<도메인>/ \
  backend/src/test/java/<도메인>/

# 본문 날짜만 검출 (javadoc 필수 태그 라인은 제외)
grep -rn "202[0-9]-[0-9]" \
  backend/src/main/java/<도메인>/ \
  | grep -v "@since\|@version"
```

---

## 7. IT 테스트 클래스 적용

테스트 메서드 주석은 *왜 검증하는가* 기준 — 시나리오 의도 + 가드. 외부 참조 금지.

```java
@Test
void searchRows_filterByCategory_shouldReturnOnlyMatching() {
    // 카테고리 필터가 적용되면 해당 카테고리 행만 반환됨을 검증.
    // fixture: rootF 직속 10건 중 짝수 5건 = "생명", 홀수 5건 = "손해".
    // 필터 누락 시 10건 전부 반환 → 회귀로 감지.
    ...
}
```

테스트 메서드 이름이 시나리오 의도를 표현하면 본문 주석은 *fixture 설명* + *실패 시 어떤 양상이 나타나는가* 위주.

---

## 적용 체크리스트 (11개)

- [ ] 클래스 javadoc — 역할 1줄, 주요 기능, (활성 조건·위임·의존성) 명시
- [ ] 클래스 javadoc — `@author` · `@since` · `@version` (필수)
- [ ] public 메서드 — `호출처:` 섹션 (Controller endpoint 또는 내부 호출 사유)
- [ ] public 메서드 — `흐름:` 섹션 (단계 + `{@link}`)
- [ ] public 메서드 — `@param` · `@return` (필수), 반환 null·빈 컬렉션 규약 명시
- [ ] public 메서드 — `@throws`는 의미 있는 도메인 예외만 (선택)
- [ ] Mapper 인터페이스 — 역할 + 반환 형태 + 계약 태그, `흐름:`은 쓰지 않음(SQL은 XML)
- [ ] private 헬퍼 — `사용처:` 명시 / 변환·매핑 헬퍼는 입출력 예시
- [ ] 인라인 박스 — 그룹 표지로만, 내용은 javadoc에
- [ ] 외부 참조(`#XXX`/`Phase N`/`결함 N`) 0건 — `grep` 검증 (`@since` 날짜는 제외)
- [ ] `{@link}` 사용 — IDE 클릭 추적 가능

---

## Token Optimization

대규모 클래스에 적용할 때 우선순위:

1. **public 메서드 javadoc 먼저** — 가장 빈번하게 IDE에서 hover로 보는 정보
2. **핵심 헬퍼** — 호출 그래프의 노드들
3. **인라인 박스 주석** — 그룹 표지로만, 자세한 내용은 javadoc에 합치기
4. **단순 헬퍼** — 마지막. 이름이 자기 설명적이면 짧게

**조기 종료 규칙**:
- DTO record / 단순 getter/setter → 주석 안 적용 (skip)
- private 메서드의 본문 코드가 5줄 미만 + 이름이 의도 명확 → javadoc 1줄로 충분

**적용 순서 (한 클래스)**:
```
1. 안티패턴 grep으로 외부 참조 검출 → 제거
2. 클래스 javadoc 작성 (역할 + 책임 범위)
3. public 메서드 순차 — 호출처 + 흐름
4. private 헬퍼 순차 — 사용처
5. 인라인 박스 정리 (javadoc과 중복 제거)
6. 컴파일 확인 + 테스트 회귀
```

---

## Related Skills

- `java-code-review` — 주석 외 일반 코드 리뷰
- `spring-boot-patterns` — Spring 컴포넌트 작성 패턴 (본 스킬과 직교)
- `test-quality` — 테스트 작성 (IT 테스트 메서드 주석은 본 스킬도 다룸)

## References

- `backend/CLAUDE.md` "주석" 섹션 — 필수 태그 규약.
- CLAUDE.md "주석" 섹션 — PR 시점 컨텍스트(현재 작업·수정 이력·이슈 번호) 참조 금지 원칙.
  (주의: 여기서 금지하는 것은 *변경을 유발한 작업/이슈* 참조이며, 본 스킬이 요구하는 `호출처:`=코드 호출 그래프,
  그리고 사내 가이드가 요구하는 `@since`=클래스 최초 작성일과는 별개다.)
- 「Backend 개발가이드 v0.51」 §코딩가이드-주석 — `@author`·`@since`·`@version`·`@param`·`@return` 필수 근거.
