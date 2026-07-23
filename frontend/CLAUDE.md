# CLAUDE.md — 프론트엔드 규약

적용 위치: `frontend/CLAUDE.md`
근거: 메인라인 WEB/SAAS 파트 「Frontend 개발가이드 v0.51」·「명명규칙 기본안 v0.50」
(Next.js 종속 항목 제외, 스택 이탈 항목은 §스택 이탈 참조)
디자인 기준은 채택하지 않았다 — §디자인 참조.

---

## 스택

| 기술 | 역할 |
|---|---|
| React + **TypeScript** | UI 구성 |
| **Vite** | 빌드 — 정적 산출물을 nginx가 서빙 |
| **React Router** | SPA 라우팅 (중첩 레이아웃) |
| **shadcn/ui + Tailwind** | UI 컴포넌트 |
| **TanStack Query** | 서버 상태 |
| **Zustand** | 클라이언트 상태 |
| **axios** | HTTP — 인터셉터에서 응답 해체 |
| **React Hook Form + Zod** | 폼·검증 |
| **react-i18next** | 다국어 (전 화면) |

### 스택 이탈 (사내 가이드와 다른 부분)

| 항목 | 사내 가이드 | ILIS |
|---|---|---|
| 프레임워크 | Next.js App Router | Vite + React Router (nginx 정적 배포) |
| UI | MUI | shadcn/ui |
| 상태 | Redux Toolkit / Zustand | TanStack Query + Zustand |
| 폼 | `useState` 수동 | React Hook Form + Zod |

가이드의 **명명 규칙·컴포넌트 분할·Layout 체계**는 그대로 채택한다. 위 4개만 이탈이다.

---

## 작업 모델 (설계 전제)

사용자는 여럿이지만 **문서 하나의 한 단계에는 한 명만 배정**된다.
입력자 → 편집자 → 검수자로 넘어가는 **직렬 단계 진행**이며, 같은 화면을 두 사람이 동시에 만지지 않는다.
저장을 누르기 전까지 DB 원본은 그대로다.

따라서 다음은 **구현하지 않는다**.

- 실시간 동기화 (WebSocket·폴링)
- 편집 충돌 해결·낙관적 락
- 창 포커스 시 자동 재조회 (`refetchOnWindowFocus: false`)

화면의 기본 필터는 **"내게 배정된 것"** 이다.

---

## 폴더 구조

```
src/
├── app/
│   ├── router.tsx              # createBrowserRouter 라우트 정의
│   └── providers.tsx           # QueryClientProvider, i18n, 테마
├── components/
│   ├── ui/                     # shadcn 컴포넌트 (button, input, table ...)
│   └── layouts/                # RootLayout, AppLayout, AuthLayout, BlankLayout
├── features/                   # 도메인 단위 — 백엔드 domain 패키지명과 일치
│   ├── law/                    # 법령
│   ├── precedent/              # 판례
│   ├── review/                 # 검수
│   └── assign/                 # 배정
│       ├── api.ts              # 이 도메인의 API 함수
│       ├── schema.ts           # Zod 스키마
│       ├── hooks.ts            # useQuery / useMutation 래퍼
│       └── components/         # 이 도메인 전용 컴포넌트
├── lib/
│   ├── api-client.ts           # axios 인스턴스 + 인터셉터
│   ├── errors.ts               # BusinessError
│   └── utils.ts
├── stores/                     # Zustand (클라이언트 상태만)
├── locales/                    # i18n 리소스 (id / en / ko)
└── types/
```

**`features/` 하위 이름은 백엔드 `domain/` 패키지명과 일치시킨다.** 화면과 API가 같은 이름으로 대응되어야 한다.

---

## 명명 규칙

| 구분 | 규칙 | 예시 |
|---|---|---|
| 폴더 | kebab-case | `features/law/components/law-table` |
| 컴포넌트 파일 | PascalCase | `LawTable.tsx`, `PrimaryButton.tsx` |
| Hooks | `useCamelCase` | `useDebounce.ts` (공용). 도메인 훅은 `features/<도메인>/hooks.ts` 에 모음 |
| `features/` 내부 파일 | 고정 이름 | `api.ts` · `schema.ts` · `hooks.ts` (폴더가 이미 도메인을 나타내므로 접두어 없음) |
| Store 파일 | camelCase + `Store` | `stores/uiStore.ts`, `stores/authStore.ts` |
| 함수·변수 | camelCase | `const lawTitle`, `getLawById()` |
| 상수 | UPPER_SNAKE_CASE | `const STATUS_OPTIONS = [...]` |
| 라우트 경로 | kebab-case | `/law-articles/:lawId` |

### Layout 명명

컴포넌트 이름은 항상 `Layout`으로 끝내고, **시각 구조가 아니라 역할**을 기준으로 짓는다.
(O: `AuthLayout` / X: `TwoColumnLayout`)

| 컴포넌트 | 역할 |
|---|---|
| `RootLayout` | 최상위 — Provider·전역 스타일 |
| `AppLayout` | 로그인 후 기본 — GNB·사이드바 포함 |
| `AuthLayout` | 로그인·가입 등 인증 화면 |
| `BlankLayout` | 공통 UI 없는 빈 화면 |
| `[Domain]Layout` | 특정 기능 그룹 하위 레이아웃 |

### 컴포넌트 배치 3분할

가이드의 `ui`/`layouts`/`features` 3분할을 따르되, 앞의 둘은 `components/` 하위에 둔다.

| 경로 | 담는 것 | 판단 기준 |
|---|---|---|
| `components/ui/` | shadcn 컴포넌트 (Button·Input·Table) | 도메인을 모르는 최소 단위 |
| `components/layouts/` | RootLayout·AppLayout·AuthLayout·BlankLayout | 페이지 뼈대 |
| `features/<도메인>/components/` | LawTable·LawCreateForm | **한 도메인에서만** 쓰임 |

두 도메인 이상에서 쓰게 되면 `components/ui/`로 올린다.

### 테이블 컴포넌트 계층

```
[데이터]Table          예: LawTable, PrecedentTable
├── TableHeader        정렬 포함
├── TableBody
│   └── TableRow
│       └── TableCell
├── TablePagination
└── TableActions       행 내부 [수정]·[삭제] 묶음
```

---

## API 계층

### 응답 해체 — 인터셉터 한 곳에서

백엔드는 모든 응답을 `{ status, code, message, data, errors }` **공통 응답 포맷**으로 내린다.
**업무 규칙 거절은 HTTP 200 + `status: "FAIL"`** 이므로, 벗기지 않고 쓰면 실패가 성공으로 처리된다.
인터셉터가 `data`만 반환하도록 벗기고, `FAIL`은 `BusinessError`로 던진다. (구현 코드는 `react-patterns` 스킬)

### 인터셉터가 하는 것 / 하지 않는 것

| | 처리 |
|---|---|
| **한다** | JWT 첨부 · 응답 해체 · 401 로그아웃·이동 · 403·5xx·네트워크 공통 토스트 · `BusinessError` 변환 |
| **하지 않는다** | 업무 거절(200+FAIL) 토스트 · 필드 오류 표시 · 화면별 분기 |

업무 거절은 화면마다 문구·동작이 다르므로 **인터셉터에서 토스트를 띄우지 않는다.**
라우터 이동은 `window.location.assign`을 쓴다 — axios 모듈이 라우터 인스턴스를 import 하면 순환 참조가 생긴다.
서버 오류는 `BusinessError`(`code`·`message`·`errors[]`·`httpStatus`)로 통일해 화면·폼이 `code`·`errors`를 쓰게 한다. (구현 코드는 `react-patterns` 스킬)

### 목록 응답 — 페이징

백엔드 목록은 `PageResponse<T>`(`content` · `page`(0-based) · `size` · `totalElements` · `totalPages`)로 온다. 인터셉터가 포맷을 벗기므로 훅이 받는 것은 이 형태다. 페이지를 이동해도 목록이 사라지지 않도록 `placeholderData: keepPreviousData`로 이전 데이터를 유지한다. 페이징 없는 짧은 목록(코드 목록 등)은 배열로 온다 — 어느 쪽인지는 백엔드 API 명세를 따른다. (인터페이스·훅 코드는 `react-patterns` 스킬)

### 날짜·시각

백엔드와 **문자열로 주고받는다**. 숫자 타임스탬프를 쓰지 않는다.
폼 값·API 페이로드는 문자열로 유지하고, `Date` 변환은 **표시 직전에만** 한다.

| 백엔드 타입 | 문자열 형식 | 표시 방법 |
|---|---|---|
| `LocalDate` | `2026-07-20` | **문자열을 직접 분해** — `new Date()` 금지 |
| `LocalDateTime` | `2026-07-20T14:30:00` | `new Date(s)` (로컬 시각으로 해석됨) |
| `OffsetDateTime` | `2026-07-20T14:30:00+07:00` | `new Date(s)` (오프셋 반영) |

### `LocalDate`에 `new Date()`를 쓰지 않는다

`new Date('2026-07-20')`은 **UTC 자정**으로 파싱된다. 음수 오프셋 지역에서는 하루 앞 날짜로 표시된다.
선고일자·공포일자처럼 시각이 없는 날짜에서 이 버그가 난다. **`LocalDate`는 문자열을 분해해(`split('-')`, month는 0-based) 로컬 `Date`로 구성**한다. 시각이 포함된 타입(`LocalDateTime`·`OffsetDateTime`)은 `new Date(s)`로 안전하다. (포맷 헬퍼 코드는 `react-patterns` 스킬)

표시 서식은 `Intl.DateTimeFormat`을 쓰고 직접 포맷 문자열을 만들지 않는다.
시간대 정책(WIB 등)은 미확정이므로 확정 후 `timeZone` 옵션을 지정한다.

### 오류 처리 책임 분담

| 처리 위치 | 대상 |
|---|---|
| 인터셉터 (공통) | 401 → 로그인 이동 / 500 → 공통 토스트 / 네트워크 오류 |
| 화면 | 업무 거절 — `error.code` 로 분기해 안내 |
| **폼** | 검증 실패 — `error.errors[]` 를 **입력칸별로** 표시 |

인터셉터는 화면에 어떤 입력칸이 있는지 모르므로 **필드 단위 오류는 폼까지 전달**한다.

---

## 서버 상태 (TanStack Query)

### 기본 설정

**자동 재조회를 전부 끄고, 갱신은 `invalidateQueries`로만 한다.** 직렬 단계 진행이라
내가 저장하지 않는 한 데이터가 바뀌지 않는다 (§작업 모델). `QueryClient` 기본값:
`refetchOnWindowFocus`·`refetchOnMount`·`refetchOnReconnect` = **모두 `false`**, `staleTime` = 5분, `retry` = 1, **mutations `retry` = 0**(중복 등록 위험). (설정 코드는 `react-patterns` 스킬)

> `refetchOnMount: false` + 긴 `staleTime`은 **변경 후 무효화를 빠뜨리면 낡은 화면이 남는다**는 뜻이다.
> mutation의 `onSuccess`에서 `invalidateQueries`를 반드시 호출한다 (아래 참조).

### queryKey 규칙

`[도메인, 식별자 또는 조건]` 순으로 짓는다(무효화 범위를 예측 가능하게). 예: `['laws']`(전체) · `['laws', { status }]`(조건부 목록) · `['law', lawId]`(단건). 도메인 훅은 `features/<도메인>/hooks.ts`에 모은다.

**저장·승인 등 변경 후에는 반드시 `invalidateQueries`로 관련 목록을 무효화한다.** 빠뜨리면 목록이 낡은 채 남는다. (`useQuery`/`useMutation` 훅 코드는 `react-patterns` 스킬)

### 금지

- **서버에서 받은 데이터를 Zustand에 복사하지 않는다.** 두 곳이 어긋난다.
- 폼 편집 중인 값을 서버 상태로 두지 않는다 — 저장 전까지는 폼 상태(RHF)다.

---

## 클라이언트 상태 (Zustand)

서버가 모르는 것만 담는다. 얇게 유지한다. (`create` 스토어 코드는 `react-patterns` 스킬)

담는 것 — 로그인 사용자·JWT, 사이드바 토글, 선택 언어, 모달 열림 여부
**담지 않는 것** — 법령 목록, 검수 대기 목록, 조문 내용 등 서버가 원본인 데이터

---

## 폼 (React Hook Form + Zod)

### 검증 스키마는 한 곳에

Zod 스키마를 백엔드 DTO와 짝이 맞게 정의하고, **타입은 스키마에서 파생**시킨다(`z.infer`). 메시지는 **i18n 키**로 둔다 (§다국어). (스키마 코드는 `react-patterns` 스킬)

### 오류 메시지 슬롯은 항상 "표시용 문구"로 통일한다

`errors.<field>.message`에는 두 출처가 섞인다 — **Zod가 만든 것**(i18n 키)과
**백엔드가 내려준 것**(이미 번역된 문구). 슬롯 의미가 갈리면 화면에서 매번 판별해야 한다.

**Zod 쪽을 번역해서 넣어 통일한다.** 앱 시작 시 `z.setErrorMap`을 i18n에 연결하면 파싱 시점에 문구가 나오고, 표시 쪽(`errors.<field>.message`)은 분기 없이 그대로 쓴다. (`setErrorMap` 코드는 `react-patterns` 스킬)

> 렌더 시점에 `t(message, { defaultValue: message })` 로 처리하지 않는다 —
> "키일 수도 문구일 수도 있다"는 상태를 화면까지 끌고 가는 방식이라 표시 코드마다 반복된다.

### 백엔드 검증 오류를 필드에 매핑

`useForm`은 `zodResolver(스키마)`로 만들고, submit에서 `BusinessError`를 잡아 공용 헬퍼(`applyServerErrors`)로 필드에 매핑한다. 알려진 필드 목록은 `Object.keys(스키마.shape)`로 만든다. (submit 코드는 `react-patterns` 스킬)

### 서버 오류 매핑은 공용 헬퍼로

`err.field`는 그냥 문자열이라 폼에 없는 이름이 올 수 있다. `as keyof` 단언으로 덮으면
**그 오류는 화면 어디에도 표시되지 않고 조용히 사라진다.** 공용 헬퍼(`applyServerErrors`)는 다음 규칙을 지킨다:

- **중첩·배열 필드는 최상위 이름으로 확인**한다 (`articles[0].title` → `articles`).
- 알려진 필드면 해당 필드에, **알 수 없는 필드는 폼 전역 오류(`root.serverError`)로** 보낸다.
- 필드 오류가 하나도 없거나(업무 거절) 매칭 안 된 것도 **폼 상단**에 표시한다.
- **필드명은 백엔드 DTO 필드명과 동일하게 맞춘다.** 다르면 전부 폼 상단으로 밀려 사용성이 나빠진다.

(헬퍼·Alert 코드는 `react-patterns` 스킬)

### 가이드 폼 명명 규약의 적용 범위

가이드의 폼 요소 명명(`selectedCategory` + `handleCategoryChange`)은 `useState` 전제라
RHF 폼 내부에는 적용되지 않는다. 범위를 나눈다.

| 구분 | 적용 |
|---|---|
| 폼 내부 | RHF 필드명이 규약을 대신 — 백엔드 DTO 필드명과 동일 |
| 폼 외부 (검색·필터 컨트롤) | 가이드 규약 유지 — `selectedStatus` + `handleStatusChange` |
| 컴포넌트명·옵션 배열 | 가이드 규약 유지 — `SelectBox`, `STATUS_OPTIONS` |

---

## 디자인 (미정)

색상 토큰·타이포그래피·화면 골격은 **이 문서에서 규정하지 않는다.**
DESIGN 개발가이드 v0.53은 MUI 테마 기준이고 본문 이하 타이포가 전부 12px로 위계가 없어
그대로 채택하지 않았다. 별도의 참고 기준이 있는지 확인 중이다.

확정 전까지는 **shadcn/ui 기본 테마**를 그대로 쓰고, 임의로 색상·크기를 정하지 않는다.
컴포넌트 사용 규약은 `shadcn-components` 스킬을 따른다.

---

## 다국어

전 화면 대상이다. 하드코딩된 문구를 남기지 않는다.

- 리소스: `src/locales/{id,en,ko}/*.json`
- 키 규칙: `<도메인>.<화면>.<요소>` — 예: `law.list.title`, `common.button.save`
- **Zod 검증 메시지도 i18n 키로 둔다** (위 §폼 참조)
- 날짜·숫자 서식은 `Intl` 사용. 직접 포맷 문자열을 만들지 않는다

### 서버 메시지의 언어 — `Accept-Language`

백엔드가 내리는 `message`·`errors[].message`는 **이미 번역된 문구**로 취급한다(§폼).
그러려면 서버가 화면 언어를 알아야 하므로 **모든 요청에 `Accept-Language`(`i18n.language` = id|en|ko)를 실어 보낸다.** (요청 인터셉터 코드는 `react-patterns` 스킬)

백엔드도 `MessageSource`로 3개 언어를 내린다(`backend/CLAUDE.md` §메시지 다국어화).
따라서 **서버 오류 문구를 프론트에서 다시 번역하지 않는다** — 받은 그대로 표시한다.

---

## 금지 사항

- 서버 데이터를 Zustand에 복사 금지
- 공통 응답 포맷(`ApiResponse`)를 컴포넌트에서 직접 해체 금지 — 인터셉터에서만
- 변경 mutation 후 `invalidateQueries` 누락 금지
- 사용자에게 보이는 문구 하드코딩 금지 (i18n 키 사용)
- `any` 사용 금지 — 불가피하면 `unknown` 후 좁히기
- 실시간 동기화·폴링 구현 금지 (§작업 모델)

---

## 관련 스킬·문서

- `react-patterns` — 인터셉터·`BusinessError`·TanStack 훅·RHF+Zod·서버오류 매핑·날짜 포맷 등 **코드 구현/예시**
- `shadcn-components` — UI 컴포넌트 설치·배치·조합
- `backend/CLAUDE.md` — API 공통 응답 포맷·오류 구조의 원천
- 「Frontend 개발가이드 v0.51」 — 명명·컴포넌트 분할·Layout 체계 근거
- 「DESIGN 개발가이드 v0.53」 — 색상·타이포·화면 골격 근거
