---
name: shadcn-components
description: shadcn/ui 컴포넌트를 ILIS 규약에 맞게 설치·사용·확장한다. 사용자가 "컴포넌트 추가", "shadcn 설치", "버튼/폼/테이블 만들어", "다이얼로그 붙여", "컴포넌트 어떻게 써", "레지스트리" 등을 요청하거나, 새 화면·폼·목록을 만들 때 적용한다. 폐쇄망 배포와 별도 관리시스템과의 컴포넌트 공유를 전제로 한다.
---

# shadcn/ui 컴포넌트 스킬 (ILIS)

ILIS 프론트엔드의 UI 컴포넌트 규약. **shadcn/ui + Tailwind** 기준이며,
컴포넌트를 새로 만들기 전에 **이미 있는 것을 먼저 확인**하는 것이 이 스킬의 목적이다.

> 상위 규약은 `frontend/CLAUDE.md`. 이 스킬은 그중 컴포넌트 부분을 상세화한다.
> 색상·타이포 토큰은 **미정**이므로 shadcn 기본 테마를 그대로 쓴다. 임의로 값을 정하지 않는다.

## When to Use

- "컴포넌트 추가해줘" / "shadcn에서 X 설치"
- 새 화면·폼·목록·모달 구현
- "이 컴포넌트 어디에 둬야 해"
- 관리시스템과 공유할 컴포넌트 정리

---

## shadcn 은 라이브러리가 아니라 소스 복사다

`npm install` 로 의존성을 추가하는 방식이 아니라, **컴포넌트 소스가 저장소 안으로 복사**된다.
따라서 다음이 성립한다.

- 설치 후 그 파일은 **우리 코드**다. 직접 수정해도 된다
- 버전 업데이트가 자동으로 오지 않는다 (의도된 동작)
- **폐쇄망에서도 동작한다** — 최초 설치 시점에만 네트워크가 필요하다

```bash
npx shadcn@latest add button input table dialog
```

`components.json` 설정에 따라 `src/components/ui/` 에 파일이 생성된다.

### 설치 전 확인

무엇이 추가·변경되는지 먼저 본다. 기존 파일을 덮어쓸 수 있다.

```bash
npx shadcn@latest add form --dry-run   # 무엇이 생기는지
npx shadcn@latest add button --diff    # 기존 파일과 차이
```

---

## ILIS 레지스트리 (공유 자산)

ILIS와 **별도 관리시스템**(권한·가입 관리)이 같은 컴포넌트를 쓰도록,
공용 컴포넌트는 사내 레지스트리로 배포한다.

- 배포 형식: `registry:base` — 컴포넌트·의존성·CSS 변수·설정을 **한 번에** 설치
- 배포 주체: ILIS 팀 (이 스킬과 함께 배포)
- 소비 주체: ILIS 프론트, 관리시스템 프론트

```bash
npx shadcn@latest add @ilis/base       # 공용 기반 일괄 설치
npx shadcn@latest add @ilis/law-table  # 개별 컴포넌트
```

### 레지스트리에 올리는 것 / 올리지 않는 것

| | 대상 |
|---|---|
| **올린다** | 두 시스템이 같이 쓰는 것 — 기본 UI 확장(폼 필드 래퍼·데이터 테이블·페이지네이션), 공통 레이아웃 뼈대, 공통 상태 배지 |
| **올리지 않는다** | 한쪽에서만 쓰는 도메인 컴포넌트 (`LawTable`·`ReviewPanel` 등), 화면별 조합물 |

**판단 기준**: "관리시스템에서도 이게 필요한가?" 아니면 `features/<도메인>/components/` 에 둔다.

> 레지스트리 구축은 **미착수**다. 그전까지는 컴포넌트를 각자 설치하고,
> 공유 대상이 생기면 이 문서에 목록을 적어둔다.

---

## 컴포넌트를 어디에 둘 것인가

새로 만들기 전에 **이 순서로 확인**한다.

```
1. components/ui/ 에 이미 있는가        → 그대로 쓴다
2. shadcn 공식에 있는가                 → npx shadcn add 로 설치
3. 두 도메인 이상에서 쓸 것인가          → components/ui/ 에 직접 작성
4. 한 도메인 전용인가                   → features/<도메인>/components/
```

| 경로 | 담는 것 | 예시 |
|---|---|---|
| `components/ui/` | 도메인을 모르는 최소 단위 | `Button`, `Input`, `Table`, `Dialog` |
| `components/layouts/` | 페이지 뼈대 | `AppLayout`, `AuthLayout` |
| `features/<도메인>/components/` | 한 도메인 전용 | `LawTable`, `PrecedentCreateForm` |

**shadcn 이 제공하는 컴포넌트를 직접 만들지 않는다.** 먼저 공식 목록을 확인한다.

---

## 자주 쓰는 조합

### 폼 — shadcn Form + React Hook Form

shadcn `form` 은 RHF 래퍼다. 별도로 붙일 필요 없이 같이 온다.

```bash
npx shadcn@latest add form input label
```

```tsx
<Form {...form}>
  <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
    <FormField
      control={form.control}
      name="lawNo"                      {/* 백엔드 DTO 필드명과 동일 */}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{t('law.field.lawNo')}</FormLabel>
          <FormControl><Input {...field} /></FormControl>
          <FormMessage />               {/* errors.lawNo.message 자동 표시 */}
        </FormItem>
      )}
    />
    <Button type="submit">{t('common.button.save')}</Button>
  </form>
</Form>
```

`FormMessage` 가 `errors.<field>.message` 를 자동으로 읽는다.
**백엔드 검증 오류를 `setError` 로 넣으면 같은 자리에 표시된다** (`frontend/CLAUDE.md` §폼 참조).

폼 전역 오류(`root.serverError`)는 `FormMessage` 가 잡지 않으므로 별도로 그린다.

```tsx
{form.formState.errors.root?.serverError && (
  <Alert variant="destructive">
    <AlertDescription>{form.formState.errors.root.serverError.message}</AlertDescription>
  </Alert>
)}
```

### 목록 — Table + 페이지네이션

백엔드는 `PageResponse<T>` 로 내린다(`page` 는 0-based). 표시용 페이지 번호와 1 차이가 나므로 주의한다.

```bash
npx shadcn@latest add table pagination badge
```

컴포넌트 계층은 `frontend/CLAUDE.md` §테이블 컴포넌트 계층을 따른다.

```
LawTable                 데이터 조회·상태 관리
├── TableHeader          정렬
├── TableBody → TableRow → TableCell
├── TablePagination      page(0-based) ↔ 표시번호(1-based) 변환
└── TableActions         행 내부 [수정]·[삭제]
```

### 상태 배지

법령·판례의 진행 단계를 `Badge` 로 표시한다. **색상을 화면마다 직접 정하지 않고** 한 곳에 모은다.

```tsx
// components/ui/status-badge.tsx
const STATUS_VARIANT = {
  DRAFT:       'secondary',
  REVIEW_WAIT: 'outline',
  REVIEW_DONE: 'default',
  SERVICE:     'default',
} as const;

export function StatusBadge({ status }: { status: keyof typeof STATUS_VARIANT }) {
  const { t } = useTranslation();
  return <Badge variant={STATUS_VARIANT[status]}>{t(`common.status.${status}`)}</Badge>;
}
```

> 상태 코드값은 백엔드 Enum 과 일치시킨다. 실제 값은 백엔드 확정 후 대조한다.

---

## 규칙

- **문구를 하드코딩하지 않는다** — 전 화면 다국어 대상이다. `t('키')` 사용
- **색상·크기를 임의로 정하지 않는다** — 토큰 미정. shadcn 기본 테마 사용
- `cn()` 유틸로 클래스를 합친다. 문자열 연결로 조건부 클래스를 만들지 않는다
- shadcn 컴포넌트를 수정할 때는 **왜 수정했는지 주석을 남긴다** (재설치 시 덮어써진다)
- 접근성 속성(`aria-*`·`role`)을 제거하지 않는다 — shadcn 기본값에 이미 들어 있다

## 금지

- shadcn 이 제공하는 컴포넌트를 직접 구현
- `components/ui/` 에 도메인 지식(법령·판례 용어)을 넣는 것
- 인라인 스타일(`style={{...}}`) — Tailwind 클래스 사용
- 유료 테마(Fuse·Mira Pro) 도입 — 라이선스 미확보

## 관련

- `frontend/CLAUDE.md` — 폴더·명명·폼·다국어 상위 규약
- shadcn 공식 문서 — https://ui.shadcn.com/docs
- 레지스트리 MCP — https://ui.shadcn.com/docs/registry/mcp
