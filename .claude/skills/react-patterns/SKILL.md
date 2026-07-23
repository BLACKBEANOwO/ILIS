---
name: react-patterns
description: React + TypeScript(Vite·React Router) 프론트엔드 모범 코드 패턴. axios 인터셉터·공통 응답 해체·BusinessError·TanStack Query 훅·React Hook Form + Zod 폼·서버오류 필드 매핑·날짜 포맷·Zustand를 만들거나 리뷰할 때 사용. 사용자가 "화면 만들어", "컴포넌트 작성", "API 훅", "폼 만들어", "인터셉터", "목록 화면", "React 코드" 등을 요청하거나 프론트 코드를 수정·리팩터·버그수정·리뷰할 때 적용. UI 컴포넌트 설치·조합은 shadcn-components 스킬. 규칙 상세는 frontend/CLAUDE.md.
---

# React Patterns Skill (Vite + TypeScript)

React + TypeScript(Vite·React Router) 프론트엔드의 코드 패턴 모음.
**규칙(명명·구조·금지·계약)의 단일 출처는 `frontend/CLAUDE.md`이고, 이 스킬은 그 규칙의 코드 구현·예시다.**

> 스택: React + TypeScript · Vite · React Router · shadcn/ui + Tailwind · TanStack Query · Zustand · axios · React Hook Form + Zod · react-i18next.
> UI 컴포넌트 설치·배치·조합은 `shadcn-components` 스킬. 규칙·금지·명명은 `frontend/CLAUDE.md`.

## When to Use
- "화면/컴포넌트 만들어" / "API 훅 작성" / "폼 만들어" / "인터셉터"
- 프론트 코드 리뷰·수정·리팩터·버그수정

---

## API 계층 — axios 인터셉터

공통 응답 포맷 `{ status, code, message, data, errors }` 를 **인터셉터 한 곳에서** 해체한다.
업무 규칙 거절은 HTTP 200 + `status: "FAIL"` 이므로, 벗기지 않고 쓰면 실패가 성공으로 처리된다. (규칙 상세는 `frontend/CLAUDE.md` §API 계층)

```ts
// lib/api-client.ts
export const apiClient = axios.create({ baseURL: '/api' });

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;      // JWT
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (res) => {
    const body = res.data as ApiResponse<unknown>;
    if (body.status === 'FAIL') {
      // HTTP 200 + FAIL = 업무 규칙 거절 → 화면이 code 로 분기 (여기서 토스트 띄우지 않음)
      throw new BusinessError(body.code, body.message, body.errors, 200);
    }
    return body.data;                                // 컴포넌트는 data 만 본다
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const body = error.response?.data as ApiResponse<unknown> | undefined;

    // 401 — 세션 만료. 스토어를 비우고 로그인으로 (라우터를 직접 import 하지 않는다)
    if (status === 401) {
      useAuthStore.getState().clear();
      window.location.assign('/login');
      return Promise.reject(new BusinessError('UNAUTHORIZED', '', undefined, 401));
    }

    // 403 — 권한 없음. 공통 안내만 (권한 판정은 백엔드 Security 소관)
    if (status === 403) {
      toast.error(i18n.t('error.forbidden'));
    }

    // 5xx·네트워크 — 공통 토스트. 화면마다 처리하지 않는다
    if (!status || status >= 500) {
      toast.error(i18n.t('error.server'));
    }

    // 공통 응답 포맷이면 같은 형태로 변환해 화면·폼이 code·errors 를 쓸 수 있게 한다
    if (body?.status === 'FAIL') {
      return Promise.reject(new BusinessError(body.code, body.message, body.errors, status));
    }
    return Promise.reject(error);
  },
);
```

> 라우터 이동은 `window.location.assign`을 쓴다 — axios 모듈이 라우터 인스턴스를 import 하면 순환 참조가 생긴다.

### BusinessError

```ts
// lib/errors.ts
export class BusinessError extends Error {
  constructor(
    public readonly code: string,          // NOT_REVIEWED, DUPLICATE_LAW_NO ...
    message: string,
    public readonly errors?: ErrorDetail[],
    public readonly httpStatus?: number,
  ) {
    super(message);
    this.name = 'BusinessError';
  }
}

export interface ErrorDetail {
  field: string;
  rejectedValue: unknown;
  message: string;
}
```

### 목록 응답 — PageResponse

백엔드 목록은 `PageResponse<T>`로 온다. 인터셉터가 포맷을 벗기므로 훅이 받는 것은 이 형태다.

```ts
export interface PageResponse<T> {
  content: T[];
  page: number;          // 0-based
  size: number;
  totalElements: number;
  totalPages: number;
}
```

페이지를 이동해도 목록이 사라지지 않도록 이전 데이터를 유지한다.

```ts
export function usePrecedentList(status: string | undefined, page: number) {
  return useQuery({
    queryKey: ['precedents', { status, page }],
    queryFn: () => precedentApi.findPrecedentsByStatus(status, page),
    placeholderData: keepPreviousData,   // 페이지 이동 시 깜빡임 방지
  });
}
```

### 날짜 — `LocalDate`에 `new Date()`를 쓰지 않는다

`new Date('2026-07-20')`은 **UTC 자정**으로 파싱되어 음수 오프셋 지역에서 하루 앞 날짜로 표시된다(선고일자·공포일자 버그). 문자열을 분해해 로컬 날짜로 만든다. (규칙은 `frontend/CLAUDE.md` §날짜·시각)

```ts
// ✗ 시간대에 따라 2026-07-19 로 표시될 수 있음
new Intl.DateTimeFormat(locale).format(new Date('2026-07-20'));

// ✓ 문자열을 분해해 로컬 날짜로 구성 (month 는 0-based)
export function formatLocalDate(value: string, locale: string): string {
  const [y, m, d] = value.split('-').map(Number);
  return new Intl.DateTimeFormat(locale).format(new Date(y, m - 1, d));
}
```
> 시각이 포함된 타입(`LocalDateTime`·`OffsetDateTime`)은 `new Date(s)`로 안전하다.

---

## 서버 상태 — TanStack Query

### QueryClient 기본 설정

자동 재조회를 전부 끄고, 갱신은 `invalidateQueries`로만 한다(직렬 단계 진행 전제). (규칙은 `frontend/CLAUDE.md` §서버 상태)

```ts
new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,   // 창 복귀 시 재조회 안 함
      refetchOnMount: false,         // 재진입 시 캐시 사용 (목록 → 상세 → 뒤로)
      refetchOnReconnect: false,     // 네트워크 복구 시 재조회 안 함
      staleTime: 5 * 60_000,         // 5분 — 갱신은 invalidate 가 담당
      retry: 1,
    },
    mutations: { retry: 0 },         // 변경은 재시도하지 않는다 (중복 등록 위험)
  },
});
```

### 조회·변경 훅

`queryKey`는 `[도메인, 식별자 또는 조건]` 순. 변경 후에는 **반드시 `invalidateQueries`로 관련 목록을 무효화**한다(빠뜨리면 목록이 낡은 채 남는다).

```ts
// features/law/hooks.ts
export function useLawList(status?: string) {
  return useQuery({
    queryKey: ['laws', { status }],
    queryFn: () => lawApi.findLawsByStatus(status),
  });
}

export function usePublishLaw() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (lawId: number) => lawApi.publishLawById(lawId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['laws'] });   // 목록 갱신
    },
  });
}
```

---

## 클라이언트 상태 — Zustand

서버가 모르는 것만 얇게 담는다(로그인 사용자·JWT·사이드바 토글·선택 언어·모달 열림). 서버가 원본인 데이터는 담지 않는다. (규칙은 `frontend/CLAUDE.md` §클라이언트 상태)

```ts
// stores/uiStore.ts
export const useUiStore = create<UiState>((set) => ({
  sidebarOpen: true,
  toggleSidebar: () => set((s) => ({ sidebarOpen: !s.sidebarOpen })),
}));
```

---

## 폼 — React Hook Form + Zod

### Zod 스키마 (검증은 한 곳에)

스키마를 백엔드 DTO와 짝 맞게 정의하고, 타입은 스키마에서 파생. 메시지는 **i18n 키**로 둔다.

```ts
// features/law/schema.ts
export const lawCreateSchema = z.object({
  lawNo: z.string().min(1, 'validation.lawNo.required'),
  title: z.string().min(1, 'validation.title.required').max(200),
});
export type LawCreateForm = z.infer<typeof lawCreateSchema>;
```

### 오류 메시지 슬롯 통일 (errorMap)

`errors.<field>.message`에는 Zod가 만든 i18n 키와 백엔드가 내려준 번역 문구가 섞인다. **Zod 쪽을 번역해서 넣어 통일**한다 — `errorMap`을 i18n에 연결하면 파싱 시점에 문구가 나온다.

```ts
// app/providers.tsx — 앱 시작 시 1회
z.setErrorMap((issue, ctx) => {
  const key = issue.message ?? ctx.defaultError;
  return { message: i18n.exists(key) ? i18n.t(key) : ctx.defaultError };
});
```

```tsx
{errors.lawNo && <p className="text-destructive">{errors.lawNo.message}</p>}
```

### 백엔드 검증 오류를 필드에 매핑

```tsx
const { register, handleSubmit, setError, formState: { errors } } =
  useForm<LawCreateForm>({ resolver: zodResolver(lawCreateSchema) });

const FIELDS = Object.keys(lawCreateSchema.shape) as (keyof LawCreateForm)[];

const onSubmit = async (values: LawCreateForm) => {
  try {
    await createLaw.mutateAsync(values);
  } catch (e) {
    if (e instanceof BusinessError) {
      applyServerErrors(e, setError, FIELDS);
      return;
    }
    throw e;
  }
};
```

### 서버 오류 매핑 헬퍼

`err.field`는 그냥 문자열이라 폼에 없는 이름이 올 수 있다. `as keyof` 단언으로 덮으면 그 오류가 조용히 사라진다. 알 수 없는 필드는 폼 전역 오류로 보낸다. **필드명은 백엔드 DTO 필드명과 동일하게 맞춘다.**

```ts
// lib/form.ts
export function applyServerErrors<T extends FieldValues>(
  error: BusinessError,
  setError: UseFormSetError<T>,
  knownFields: readonly (keyof T)[],
) {
  const unknown: string[] = [];

  for (const detail of error.errors ?? []) {
    // 중첩·배열 필드는 최상위 이름으로 확인 (articles[0].title → articles)
    const root = detail.field.split(/[.[]/)[0];
    if ((knownFields as readonly string[]).includes(root)) {
      setError(detail.field as Path<T>, { message: detail.message });
    } else {
      unknown.push(detail.message);
    }
  }

  // 필드 오류가 하나도 없거나(업무 거절) 매칭 안 된 것은 폼 상단에
  if (!error.errors?.length || unknown.length) {
    setError('root.serverError', {
      message: unknown.join('\n') || error.message,
    });
  }
}
```

```tsx
{errors.root?.serverError && (
  <Alert variant="destructive">{errors.root.serverError.message}</Alert>
)}
```

---

## 다국어 — `Accept-Language`

백엔드가 내리는 `message`·`errors[].message`는 **이미 번역된 문구**로 취급한다. 서버가 화면 언어를 알도록 **모든 요청에 `Accept-Language`를 실어 보낸다**. 서버 오류 문구를 프론트에서 다시 번역하지 않는다. (규칙은 `frontend/CLAUDE.md` §다국어)

```ts
apiClient.interceptors.request.use((config) => {
  config.headers['Accept-Language'] = i18n.language;   // id | en | ko
  return config;
});
```

---

## 관련

- `frontend/CLAUDE.md` — 명명·폴더 구조·계층 책임·금지·계약(규칙의 단일 출처)
- `shadcn-components` — UI 컴포넌트 설치·배치·조합
- `backend/CLAUDE.md` — 공통 응답 포맷·오류 구조의 원천
