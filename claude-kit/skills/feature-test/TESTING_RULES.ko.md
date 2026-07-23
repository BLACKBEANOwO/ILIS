# Testing Rules (한국어 참고용)

> 이 파일은 **사람이 읽기 위한 한국어 번역본**입니다. 정본은 [SKILL.md](./SKILL.md)의 Testing Rules 섹션(영문)이며, 스킬 실행 시 적용되는 규칙은 영문 원문입니다.

---

이 저장소에서 테스트를 작성하거나 수정하는 모든 에이전트(Claude Code, Codex, Cursor)를 위한 규칙입니다.

## 핵심 철학

1. 구현이 아닌 **동작**을 테스트한다. 순수 리팩토링으로 테스트가 깨져서는 안 된다.
2. **시스템 경계**에서만 mock한다. 내부는 모두 실제 객체다.
3. **Classist(Chicago) TDD**를 선호한다. Mockist(London)는 AI 주도 코드베이스에서 빠르게 썩는다.
4. 의미 있는 테스트 소수가 허술한 테스트 다수보다 낫다.

## Mock 규칙

**Mock해야 하는 대상** — 오직 이것들만:
- 데이터베이스 / ORM
- 서드파티 HTTP API
- 파일시스템, 시계(clock), 난수, 네트워크
- 프로세스 경계를 넘는 모든 것

**절대 Mock하지 말 것:**
- 값 객체, DTO, 직접 소유한 엔티티
- 순수 함수와 유틸리티
- 내부 협력자 (같은 코드베이스의 서비스/모듈)
- 테스트 대상 단위 자체 (그러고 싶다면 단위 경계가 잘못된 것)

trait/interface mock보다 **HTTP 레벨 fake**(`wiremock`, `msw`, `nock` 등)를 선호한다.
`fs` mock보다 **실제 임시 파일시스템**(`tempfile`, `tmp.dirSync()`)을 선호한다.

## 어서션 규칙

- **반환값**과 **관찰 가능한 상태**를 검증한다.
- `toHaveBeenCalledWith(...)` / `verify(...)` / `expect(spy).toBe(...)`를 주된 검증 수단으로 삼지 않는다.
- 필드별 비교 대신 전체 객체를 비교한다 (`expect(result).toEqual(expected)`).
- 비결정적 출력(LLM 텍스트, 타임스탬프, 순서 없는 집합)은 절대 snapshot하지 않는다.

## 네이밍 규칙

테스트 이름은 **관찰 가능한 동작**을 서술한다. 메서드 이름이나 내부 호출을 쓰지 않는다.

```
// 나쁨 — 구현 냄새
test_findUnique_called_once()
test_calls_upsert_then_emits_event()
should_work()

// 좋음 — 동작
returns_cached_result_when_fetched_within_ttl()
rejects_login_when_password_is_expired()
charges_full_price_for_non_vip_users()
```

템플릿: `<주체>_<기대 동작>_when_<조건>`

## 구조 규칙

| 레이어 | 목적 | 예산 |
|---|---|---|
| Unit | 순수 로직, 엔티티, 유틸 | 다수, 인메모리, 밀리초 단위 |
| Integration | 모듈 + 실제 DB/큐 | 중간, 중요 모듈당 |
| E2E | 핵심 사용자 여정 | 소수, 여정당 1개 |
| Regression | 과거 장애당 1개 | 버그 발생 시 |

- 핵심 여정당 E2E 1개. 도메인당 통합 테스트 몇 개.
- 단위 테스트는 로직이 자명하지 않은 곳에만. getter, DI 배선, 프레임워크 glue에는 단위 테스트를 두지 않는다.
- Unit spec은 소스 파일 옆에 colocate한다. Integration/E2E는 별도 트리에 둔다.
- 비싼 live 테스트는 env flag(`LIVE_TEST=true`, `RUN_EXPENSIVE=1`)로 게이트한다.

## 도메인 엔티티 규칙

다음 중 **하나라도** 해당하면 도메인 엔티티를 추출한다:
- 같은 데이터에 대한 비즈니스 로직이 2개 이상의 서비스에 흩어져 있다.
- 서비스가 순수한 DB row에 대해 산술/상태 전이를 수행한다.
- 사실 순수 로직인데 테스트하려고 DB를 띄워야 한다.

```
# Before — 로직이 서비스에 있고 ORM에 묶임
user.hunger = user.hunger - EAT * 2
user.energy = user.energy + SLEEP * 2
db.user.update(user)

# After — 로직은 엔티티, 서비스는 영속화만
user.eat()
user.sleep()
user_repo.save(user)
```

그러면 `User.eat()`은 순수 인메모리 단위 테스트가 된다. 밀리초, mock 없음, drift 없음.

## 속성 기반 테스트 (Property-Based Testing)

큰 입력 공간에 대해 명확한 **불변식**이 있는 대상(파서, 인코더, 정렬기, 검증기, 상태 머신)은 example 테스트에 더해 속성 기반 테스트를 쓴다. 라이브러리: `fast-check`(TS), `hypothesis`(Python), `proptest`(Rust).

규칙: 같은 함수에 대한 네 번째 example 테스트를 쓰고 있다면, 속성 테스트로 전환한다.

## Flaky 테스트 규칙

1. Flaky 테스트를 절대 커밋하지 않는다. 들어왔다면 24시간 내에 격리한다.
2. **격리**란: 연결된 issue, 담당자, 기한과 함께 skip. 담당자 없으면 삭제.
3. Flakiness는 **근본 원인**에서 고친다 — retry loop, `sleep()`, 타임아웃 증가로 해결하지 않는다.
4. 흔한 원인: 공유 전역 상태, 실제 시계, 테스트 순서, seed 없는 난수, 네트워크. **증상이 아니라 원인**을 고친다.

## 마이그레이션 규칙 (기존 Mockist 코드베이스)

재미로 기존 테스트를 다시 쓰지 않는다. 점진적으로 적용한다:

1. **오늘부터의 새 테스트**는 이 규칙을 완전히 따른다.
2. **수정하는 파일**: 테스트를 편집할 때 경계의 mock만 변환한다.
3. **최악의 오염원부터**: `toHaveBeenCalledWith`가 가장 많은 파일 3-5개를 식별해 도메인 단위로 하나씩 재작성한다.
4. **위험도 높은 도메인 하나**에 먼저 실제 DB(Testcontainers / docker-compose)를 도입한다. 패턴이 입증된 뒤에만 확장한다.
5. 비결정적 출력에 대한 snapshot 테스트는 삭제한다. 구조적 어서션으로 대체하거나 삭제한다.

## 워크플로 규칙

- 스펙으로부터 **실패하는 테스트 먼저** 작성하고, 그에 맞춰 구현한다.
- 코드를 먼저 생성한 뒤 에이전트에게 "이 파일에 대한 테스트를 작성하라"고 시키지 않는다 — 이는 현재 구현에 묶인 coverage theater를 낳는다.
- **테스트 하나당 동작 하나**. 하나의 동작을 서술하기 위해 `expect()` 3개가 필요한 건 괜찮다; 세 가지 동작을 테스트하고 있다면 세 테스트로 분리한다.

## PR 레드 플래그 — 거절 또는 재작업

- 실제 어서션보다 `mock.*` 호출이 더 많다.
- `toHaveBeenCalledWith` / `verify()`가 유일한 어서션이다.
- `_internal/`이나 private 모듈 경로를 import한다.
- LLM, 타임스탬프, 네트워크 출력의 snapshot이 있다.
- 연결된 issue와 담당자 없이 `it.skip`을 쓴다.
- 테스트 대상 함수 이름이 바뀔 때마다 테스트 이름이 바뀐다 (누수).
- public 함수 하나짜리 파일인데 테스트 파일이 원본보다 길다.
- 경계 mock이나 실제 DB 대신 새 `mockall` / 전체 prisma-mock이 추가되었다.

## 테스트를 **쓰지 말아야** 할 때

- 로직 없는 단순 CRUD → E2E 하나로 커버된다.
- 프레임워크 배선(DI, 라우팅, 모듈) → 프레임워크가 테스트한다.
- Config / 상수 → 타입 시스템이나 스키마 검증기가 테스트한다.
- 일회성 스크립트 → 프로덕션 데이터를 건드리는 경우가 아니라면.
- 곧 삭제할 코드.

테스트가 보호하는 동작을 **한 문장으로** 말할 수 없다면, 쓰지 않는다.

## 한 줄 요약

> 테스트로부터 구현을 숨겨라. 구현으로부터 테스트를 숨겨라.
> 오직 **동작**만이 둘을 잇는다.
