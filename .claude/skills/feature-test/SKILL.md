---
name: feature-test
description: 구현된 기능을 브라우저 자동화로 테스트한다. 사용자가 "테스트 해줘", "브라우저 테스트", "feature test", "화면 테스트", "E2E 테스트" 등을 요청할 때 사용. 기능명세서의 검증 시나리오/사용자 시나리오를 기반으로 테스트 시나리오를 생성하고, Playwright/Chrome DevTools MCP로 실행하여 오류 0건까지 수정 루프를 돌린 뒤 최종 보고서를 생성한다.
---

# 기능 테스트 스킬

---

## Testing Rules (최우선 — 모든 절차는 이 규칙 아래에서 동작한다)

> 이 섹션은 본 스킬의 **최상위 원칙**이다. 아래 실행 절차와 충돌하면 이 섹션이 우선한다.
>
> 한국어 참고용 번역본: [TESTING_RULES.ko.md](./TESTING_RULES.ko.md) (사람 열람용, 에이전트는 자동 로드하지 않음)

Rules for any agent (Claude Code, Codex, Cursor) writing or modifying tests in this repo.
Drop this file at the project root. Agents read it automatically.

### Core Philosophy

1. Test behavior, not implementation. Pure refactors must not break tests.
2. Mock only at the system boundary. Everything inside is real.
3. Prefer Classist (Chicago) TDD. Mockist (London) rots fast in AI-driven codebases.
4. Fewer meaningful tests beat many leaky ones.

### Mocking Rules

**Mock these** — and only these:
- Database / ORM
- Third-party HTTP APIs
- Filesystem, clock, randomness, network
- Anything crossing a process boundary

**Never mock these:**
- Value objects, DTOs, entities you own
- Pure functions and utilities
- Internal collaborators (services/modules in the same codebase)
- The unit under test (if tempted, your unit boundary is wrong)

Prefer an HTTP-level fake (e.g. `wiremock`, `msw`, `nock`) over a trait/interface mock.
Prefer a real temp filesystem (`tempfile`, `tmp.dirSync()`) over a mocked `fs`.

### Assertion Rules

- Assert on **return values** and **observable state**.
- Do not make `toHaveBeenCalledWith(...)` / `verify(...)` / `expect(spy).toBe(...)` the primary verification.
- Compare whole objects over field-by-field assertions (`expect(result).toEqual(expected)`).
- Never snapshot non-deterministic output (LLM text, timestamps, ordering-free sets).

### Naming Rules

Test names state observable behavior. Never method names or internal calls.

```
// Bad — implementation-flavored
test_findUnique_called_once()
test_calls_upsert_then_emits_event()
should_work()

// Good — behavior
returns_cached_result_when_fetched_within_ttl()
rejects_login_when_password_is_expired()
charges_full_price_for_non_vip_users()
```

Template: `<subject>_<expected_behavior>_when_<condition>`

### Structure Rules

| Layer | Purpose | Budget |
|---|---|---|
| Unit | Pure logic, entities, utils | Many, in-memory, milliseconds |
| Integration | Module + real DB/queue | Moderate, per critical module |
| E2E | Critical user journeys | Few, one per journey |
| Regression | One per past incident | As bugs happen |

- One E2E per critical journey. A handful of integration tests per domain.
- Unit tests only where logic is non-trivial. No unit tests for getters, DI wiring, or framework glue.
- Colocate unit specs next to source. Keep integration/E2E in a separate tree.
- Gate expensive live tests behind an env flag (`LIVE_TEST=true`, `RUN_EXPENSIVE=1`).

### Domain Entity Rules

Extract a domain entity when **any** of these are true:
- Business logic is scattered across 2+ services on the same data.
- A service does arithmetic or state transitions on a plain DB row.
- You need to spin up a DB to test logic that is secretly pure.

```
# Before — logic in the service, tied to ORM
user.hunger = user.hunger - EAT * 2
user.energy = user.energy + SLEEP * 2
db.user.update(user)

# After — logic in the entity, service only persists
user.eat()
user.sleep()
user_repo.save(user)
```

Then `User.eat()` is a pure in-memory unit test. Milliseconds, no mocks, no drift.

### Property-Based Testing

For anything with a clear invariant over a large input space (parsers, encoders,
sorters, validators, state machines), use property-based tests in addition to
example tests. Libraries: `fast-check` (TS), `hypothesis` (Python), `proptest` (Rust).

Rule: if you're writing the 4th example test for the same function, switch to a property.

### Flaky Test Rules

1. Never commit a flaky test. If one lands, quarantine within 24h.
2. Quarantine means: skip with a linked issue, owner, and deadline. No owner = delete.
3. Fix flakiness at the root — never by retry loops, `sleep()`, or higher timeouts.
4. Common roots: shared global state, real clock, test ordering, unseeded randomness, network. Fix the root, not the symptom.

### Migration Rules (existing Mockist codebase)

Do not rewrite existing tests for sport. Apply incrementally:

1. **New tests** from today onward follow these rules fully.
2. **Touched files**: when editing a test, convert its mocks at the boundary only.
3. **Worst offenders first**: identify top 3-5 files with the most `toHaveBeenCalledWith` — rewrite those one domain at a time.
4. Introduce a real database (Testcontainers / docker-compose) for **one** high-risk domain first. Expand only after the pattern is proven.
5. Delete snapshot tests on non-deterministic outputs. Replace with structural assertions or delete.

### Workflow Rules

- Write the failing test from the spec **first**, then implement against it.
- Never generate code first and ask an agent to "write tests for this file" — that produces coverage theater locked to the current implementation.
- One behavior per test. If you need three `expect()` to describe one behavior that is fine; if you are testing three behaviors, split into three tests.

### PR Red Flags — Reject or Rework

- More `mock.*` calls than real assertions.
- `toHaveBeenCalledWith` / `verify()` as the only assertion.
- Imports reaching into `_internal/` or private module paths.
- Snapshots of LLM, timestamp, or network output.
- `it.skip` without a linked issue and owner.
- Tests renamed every time the function under test is renamed (leakage).
- A test file longer than the file it tests, for a file with one public function.
- New `mockall` / full-prisma-mock added instead of boundary mock or real DB.

### When NOT to Write a Test

- Plain CRUD with no logic → one E2E covers it.
- Framework wiring (DI, routing, modules) → framework tests it.
- Config / constants → type system or schema validator tests them.
- Throwaway scripts → unless they touch production data.
- Code you are about to delete.

If you cannot state the behavior the test protects in one sentence, do not write it.

### One Line to Remember

> Hide the implementation from the test. Hide the test from the implementation.
> Only behavior connects them.

---

## 목적

기능명세서의 **검증 시나리오(§2)** 와 **사용자 시나리오(§3)** 를 기반으로 테스트 시나리오를 작성하고, 브라우저 자동화(Playwright/Chrome DevTools MCP)로 실행하여 **오류 완전 제거 후** 최종 보고서를 생성한다.

모든 시나리오 작성·실행·판정은 위 **Testing Rules**에 종속된다.

## 저장 경로 규칙 (CLAUDE.md 준수)

- 시나리오·보고서 모두 `docs/<현재브랜치>/` 하위에 저장한다.
  - `<현재브랜치>`는 `git branch --show-current` 결과를 그대로 사용한다 (슬래시 포함 시 중첩 폴더).
  - 폴더가 없으면 `mkdir -p`로 생성한다.
- 스킬 시작 시 **가장 먼저** 현재 브랜치를 조회해 저장 경로를 확정한다.

## 진행 규칙 (중요)

- **다른 정보의 md 파일을 읽을 때는 사용자의 확인을 받는다**. 단, 사용자가 지정한 기능명세서 파일은 예외다.
- 수정 모드 기본값: **승인 필요** — 오류 발견 시 수정안을 보여주고 사용자 확인 후 수정한다.
- 사용자가 "확인 필요없다" / "알아서 수정해" 등을 지시하면 **자동 수정 모드**로 전환하여 루프 내에서 자체 수정한다.
- 모드 전환은 **명시적 신호가 있을 때만**. 추측으로 모드를 바꾸지 않는다.

## 실행 절차

### 단계 1 — 현재 브랜치 확인 및 사용자 질의

1. `git branch --show-current`로 현재 브랜치를 조회해 저장 경로(`docs/<현재브랜치>/`)를 결정한다. 필요 시 `mkdir -p`.
2. 이어서 사용자에게 다음을 확인한다 (이미 알려진 정보는 건너뛴다).

| 항목 | 필수 | 기본값 |
|---|---|---|
| 테스트 대상 페이지 URL | 필수 | - |
| 참고할 기능명세서 경로 | **선택** | - |
| 로그인 필요 여부 | 필수 | - |
| 테스트 계정 (ID / PW) | 로그인 필요 시 필수 | - |
| 수정 모드 (승인/자동) | 선택 | 승인 |

> **명세서가 없어도 동작한다.** Claude가 직접 구현한 경우, 대화 맥락과 구현 코드에서 시나리오를 자동 생성한다.

### 단계 2 — 테스트 시나리오 작성

> 준수: Testing Rules §Naming · §Structure · §When NOT to Write a Test. (한국어 혼용 시에도 템플릿 구조 유지)

#### 경로 A: 기능명세서가 있는 경우

1. 기능명세서 읽기
2. §2 Backend API의 **검증 시나리오 표** 에서 API 레벨 테스트 케이스 추출
3. §3 Frontend의 **사용자 시나리오 표** 에서 E2E 레벨 테스트 케이스 추출

#### 경로 B: 기능명세서가 없는 경우 (Claude가 직접 구현한 경우)

1. **구현 코드 분석** — API 엔드포인트(Controller/Router), 화면 컴포넌트, 폼 검증 로직을 읽는다
2. **대화 맥락 참조** — 현재 세션에서 구현한 기능의 요구사항·동작 흐름을 기반으로 시나리오를 추론한다
3. **자동 추출 대상**:
   - API 엔드포인트별 정상/에지/거부 케이스 (Controller의 Validation, Exception Handler 참조)
   - 화면 동작별 정상/에지/거부 케이스 (컴포넌트의 이벤트 핸들러, 에러 처리 참조)
   - 폼 필수값/길이 제한 (React 폼 검증 로직 + Bean Validation 참조)

#### 공통

4. 시나리오를 다음 형식으로 정리하여 사용자에게 **검토 요청**

```markdown
## 테스트 시나리오

### API 테스트 (§2 기반)

| # | API | 구분 | 시나리오 (네이밍 템플릿 준수) | 기대 결과 |
|---|---|---|---|---|
| 1 | GET /api/... | 정상 | returns_list_when_valid_params | 200 + ... |
| 2 | ... | 에지 | ... | ... |
| 3 | ... | 거부 | rejects_request_when_missing_required_field | 400 + ... |

### E2E 테스트 (§3 기반) — 핵심 여정당 1개

| # | 기능 | 구분 | 사용자 액션 | 기대 화면 반응 |
|---|---|---|---|---|
| 1 | 조회 | 정상 | ... | 그리드 로드 + ... |
| 2 | ... | 에지 | ... | ... |
| 3 | ... | 거부 | ... | 에러 토스트 + ... |
```

5. 사용자 확인 후 `docs/<현재브랜치>/test_scenario_<기능명>.md` 로 저장
6. 시나리오 추가/제거 요청이 있으면 반영 후 재확인

### 단계 3 — 실행 환경 확인

1. **프론트엔드 개발 서버** 기동 상태 확인 (실행 명령·포트는 프로젝트 규약을 따른다 — 예: `npm run dev` / `pnpm dev`. 값을 임의로 발명하지 말고 미확인 시 사용자에게 확인한다)
2. **백엔드 서버** 기동 상태 확인 (Maven 기준 `./mvnw spring-boot:run`. 멀티모듈·wrapper 미사용·별도 profile/port·기존 run 스크립트 등 프로젝트 사정이 다를 수 있으므로, 미확인 시 실행 명령·포트를 사용자에게 확인한다)
3. 미기동 시 사용자에게 "지금 기동할까요?" 질의 후 기동
4. 로그인 필요 시 해당 프로젝트의 로그인/인증 흐름을 브라우저 자동화로 처리 (테스트 계정 사용)

### 단계 4 — 테스트 실행 루프 (오류 0건까지)

> 준수: Testing Rules §Assertion · §Flaky Test · §PR Red Flags.

```
루프 시작:
  1. Playwright 또는 Chrome DevTools MCP로 시나리오 실행 + 주요 단계 스크린샷
  2. 결과 수집: 시나리오별 pass/fail, 콘솔 에러, 네트워크 4xx/5xx
  3. 통과 기준: 모든 시나리오 pass + 콘솔 에러 0건 + 의도치 않은 4xx/5xx 0건
     (의도된 거부 테스트의 4xx는 pass로 판정)
  4. 오류 있으면 근본 원인 분석 후 수정
     - 승인 모드: 수정안(파일 경로 + 변경 내용) 제시 후 사용자 확인
     - 자동 모드: 즉시 적용
     → 다시 1번으로
  5. 오류 없으면 루프 탈출
```

### 단계 5 — 최종 보고서 생성

`docs/<현재브랜치>/test_report_<기능명>.md` 로 저장한다.

**보고서 포함 항목:**

```markdown
# <기능명> 테스트 보고서

| 항목 | 값 |
|---|---|
| 테스트 대상 | (페이지 URL) |
| 테스트 일시 | YYYY-MM-DD HH:mm |
| 참조 명세서 | (기능명세서 경로) |
| 참조 시나리오 | (시나리오 파일 경로) |
| 총 시나리오 | N건 |
| 최종 결과 | 전체 pass / N건 fail |

## 시나리오별 결과

| # | 시나리오 | 구분 | 결과 | 비고 |
|---|---|---|---|---|
| 1 | ... | 정상 | ✅ pass | |
| 2 | ... | 에지 | ✅ pass | |
| 3 | ... | 거부 | ✅ pass | 1차 실패 → 수정 후 통과 |

## 수정 이력

| # | 발견 오류 | 원인 | 수정 파일 | 수정 내용 |
|---|---|---|---|---|
| 1 | (에러 설명) | (근본 원인) | (파일 경로) | (변경 요약) |

## Testing Rules 자가 점검

- [ ] Testing Rules §PR Red Flags 전 항목 준수

## 스크린샷

- (주요 단계별 스크린샷 경로 또는 인라인 이미지)

## 잔여 이슈

- (오류는 아니지만 확인이 필요한 사항)
- (성능 우려, 접근성 미비 등)
```

### 단계 6 — 완료 보고

사용자에게 다음을 안내한다.

```
테스트가 완료되었습니다.
- 시나리오: docs/<현재브랜치>/test_scenario_<기능명>.md
- 보고서: docs/<현재브랜치>/test_report_<기능명>.md
- 결과: 전체 N건 중 N건 pass (오류 0건)
- 수정 이력: M건
- Testing Rules 자가 점검: 전 항목 통과

커밋이 필요하면 요청해 주세요.
```

## 산출물

- `docs/<현재브랜치>/test_scenario_<기능명>.md` — 테스트 시나리오
- `docs/<현재브랜치>/test_report_<기능명>.md` — 최종 보고서 (자가 점검 포함)

## 브라우저 자동화 도구 선택

| 도구 | 용도 |
|---|---|
| **Playwright MCP** | E2E 테스트 (클릭/입력/검증 중심) — 기본 도구 |
| **Chrome DevTools MCP** | 콘솔 메시지/네트워크 관찰, 성능 분석이 필요할 때 보조 사용 |

> 두 도구를 병행 사용할 수 있다. E2E 흐름은 Playwright, 콘솔/네트워크 모니터링은 Chrome DevTools.

## 주의사항

- **요청 범위를 벗어난 수정은 하지 않는다** — 테스트에서 발견된 오류만 수정하며, 관련 없는 리팩토링/개선을 하지 않는다.
- **추측으로 수정하지 않는다** — 오류 원인이 불명확하면 로그/코드를 추가 분석하거나 사용자에게 질문한다.
- **보안 민감 정보** — 테스트 계정 ID/PW는 보고서에 기재하지 않는다.
- **스크린샷** — 개인정보가 포함된 화면은 마스킹 후 보고서에 첨부한다.
