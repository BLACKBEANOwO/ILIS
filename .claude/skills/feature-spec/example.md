# 공지사항 관리 기능명세서

> ⚠️ **이 문서는 `feature-spec` 스킬의 공식 참조 샘플입니다.**
> 실제 운영 기능이 아니며, `./output_format.md` 포맷 규칙의 모든 필수 필드를 완결형으로 보여주기 위한 **가상 기능** 기준으로 작성되었습니다.
> 새 기능명세서를 작성할 때는 **이 문서의 구조·깊이·표기 스타일을 기준점으로 삼습니다.**

| 항목 | 값 |
|---|---|
| 기능명 | 공지사항 관리 |
| 스코프 | full |
| 작성일 | 2026-04-17 |
| 상태 | 확정 (샘플) |
| 관련 테이블 | `notice` |
| 관련 API | `/api/notices`, `/api/notices/{id}` |
| 관련 구현 경로 | `frontend/src/features/notice`, `backend`의 `NoticeController`/`NoticeService`/`NoticeMapper` + `src/main/resources/mapper/NoticeMapper.xml` |
| 참고 문서 | `.claude/skills/feature-spec/output_format.md` (포맷 규칙) |

---

## 데이터 모델 (§2) — 트리/데이터 구조

### 계층 구조

```
notice (공지사항 마스터)
├─ 일반 공지 (pinned=false)
└─ 상단 고정 공지 (pinned=true) — 목록 최상단에 강조 표시
```

- 공지사항은 단일 테이블로 관리 (계층 구조 없음)
- `pinned=true` 항목은 목록에서 상단에 먼저 노출
- `published=false` 항목은 작성자에게만 보이는 비공개 초안

### 상세 테이블 매핑

| 유형 | 테이블 | 조건 |
|---|---|---|
| 공지 목록 | `notice` | `published = true` (관리자 전체 조회 시 제외) |
| 고정 공지 | `notice` | `pinned = true AND published = true` |
| 비공개 초안 | `notice` | `published = false` — 작성자 본인에게만 조회 허용 |

### 테이블: notice

| 컬럼 | 타입 | 설명 | 역할 |
|---|---|---|---|
| `notice_id` | BIGSERIAL PK | 공지 고유 ID | 식별자 |
| `title` | VARCHAR(200) NOT NULL | 공지 제목 | 목록/상세 표시 |
| `content` | TEXT NOT NULL | 공지 본문 | 상세 표시 (Markdown 지원) |
| `pinned` | BOOLEAN NOT NULL DEFAULT FALSE | 상단 고정 여부 | 목록 정렬에 사용 |
| `published` | BOOLEAN NOT NULL DEFAULT TRUE | 게시 여부 | false=비공개 초안 |
| `view_count` | BIGINT NOT NULL DEFAULT 0 | 조회수 | 상세 조회 시 +1 |
| `creator` | VARCHAR(50) NOT NULL | 생성자 사용자 ID | JWT에서 자동 기입 |
| `created` | TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP | 생성일시 | |
| `modifier` | VARCHAR(50) | 최종 수정자 ID | 수정 시 JWT에서 기입 |
| `modified` | TIMESTAMP | 최종 수정일시 | 수정 시 서버 시간 |

**인덱스**:
- `pk_notice` — PK (`notice_id`)
- `idx_notice_published_created` — (`published`, `created` DESC) — 목록 정렬용
- `idx_notice_pinned_created` — (`pinned` DESC, `created` DESC) — 고정 우선 정렬용

---

## API 스펙 (§5) — Backend API

### 5-1. 공지사항 목록 조회

```
GET /api/notices?page={page}&size={size}&keyword={keyword}
Authorization: Bearer {JWT}
```

- `page`: 0부터 시작 (기본 0)
- `size`: 페이지당 건수 (기본 20, 최대 100)
- `keyword`: 제목 부분 일치 검색 (선택)

```sql
SELECT notice_id, title, pinned, published, view_count,
       creator, created, modifier, modified
FROM notice
WHERE published = true
  AND (:keyword IS NULL OR title ILIKE '%' || :keyword || '%')
ORDER BY pinned DESC, created DESC
LIMIT :size OFFSET (:page * :size);
```

**Response:**
```json
{
  "items": [
    {
      "noticeId": 42,
      "title": "시스템 점검 안내",
      "pinned": true,
      "published": true,
      "viewCount": 1523,
      "creator": "admin01",
      "created": "2026-04-15T09:30:00Z",
      "modifier": null,
      "modified": null
    }
  ],
  "page": 0,
  "size": 20,
  "totalCount": 57,
  "totalPages": 3
}
```

**처리 순서 (단일 트랜잭션 불필요 — 조회):**
1. 쿼리 파라미터 검증 (page ≥ 0, 1 ≤ size ≤ 100)
2. `published = true` 조건으로 조회 (관리자 여부와 무관하게 일반 조회는 게시된 항목만)
3. 고정 항목 우선, 그 외 최신순 정렬
4. 페이징 적용

**에러 응답**

| 조건 | Status | 에러 코드 | 메시지 |
|---|---|---|---|
| size가 범위 밖 (0 이하 또는 100 초과) | 400 | INVALID_PAGE_SIZE | "size는 1~100 사이여야 합니다" |
| page가 음수 | 400 | INVALID_PAGE | "page는 0 이상이어야 합니다" |
| JWT 누락/만료 | 401 | UNAUTHORIZED | "인증이 필요합니다" |
| DB 오류 | 500 | INTERNAL_ERROR | "일시적 오류입니다" |

**검증 시나리오**

| 구분 | 시나리오 | 입력 | 기대 결과 |
|---|---|---|---|
| 정상 | 기본 조회 | `?page=0&size=20` | 200 + 고정 항목 우선, 최신순 |
| 정상 | 키워드 검색 적중 | `?keyword=점검` | 200 + title에 "점검" 포함 항목만 |
| 정상 | 결과 0건 | `?keyword=존재하지않는문구` | 200 + items: [] + totalCount: 0 |
| 에지 | 경계값 | `?size=100` | 200 + 최대 100건 반환 |
| 에지 | 마지막 페이지 부분 결과 | `?page=마지막&size=20` | 200 + 남은 건수만큼 반환 |
| 거부 | size 초과 | `?size=101` | 400 INVALID_PAGE_SIZE |
| 거부 | 토큰 없음 | 헤더 누락 | 401 UNAUTHORIZED |

**규칙 요약**
- 일반 조회는 `published=true`만 반환 (비공개 초안 노출 금지)
- 정렬 우선순위: 고정(pinned) DESC → 작성일(created) DESC
- 페이지 크기 상한 100 — 과도한 로드 방지

---

### 5-2. 공지사항 상세 조회

```
GET /api/notices/{noticeId}
Authorization: Bearer {JWT}
```

```sql
-- 본문 조회
SELECT notice_id, title, content, pinned, published, view_count,
       creator, created, modifier, modified
FROM notice
WHERE notice_id = :noticeId
  AND (published = true OR creator = :currentUserId);

-- 조회수 +1 (본인 조회는 제외)
UPDATE notice SET view_count = view_count + 1
WHERE notice_id = :noticeId AND creator <> :currentUserId;
```

**Response:**
```json
{
  "noticeId": 42,
  "title": "시스템 점검 안내",
  "content": "## 점검 일정\n2026-04-20 02:00 ~ 04:00",
  "pinned": true,
  "published": true,
  "viewCount": 1524,
  "creator": "admin01",
  "created": "2026-04-15T09:30:00Z",
  "modifier": null,
  "modified": null
}
```

**처리 순서 (단일 트랜잭션):**
1. 본문 조회 (비공개 항목이면 작성자 본인만 허용)
2. 조회수 +1 (작성자 본인 조회는 증가 안 함)
3. 둘을 한 트랜잭션으로 묶어 일관성 유지

**에러 응답**

| 조건 | Status | 에러 코드 | 메시지 |
|---|---|---|---|
| 대상 없음 | 404 | NOTICE_NOT_FOUND | "공지사항을 찾을 수 없습니다" |
| 비공개 항목을 타인이 조회 (존재 은폐) | 404 | NOTICE_NOT_FOUND | "공지사항을 찾을 수 없습니다" |
| 토큰 누락/만료 | 401 | UNAUTHORIZED | "인증이 필요합니다" |
| DB 오류 | 500 | INTERNAL_ERROR | "일시적 오류입니다" |

**검증 시나리오**

| 구분 | 시나리오 | 입력 | 기대 결과 |
|---|---|---|---|
| 정상 | 공개 공지 조회 | 유효 id | 200 + 본문 + viewCount +1 |
| 정상 | 본인의 비공개 초안 조회 | 본인이 작성한 draft id | 200 + 본문 + viewCount 유지 |
| 에지 | 본인 조회 시 viewCount 유지 | 자기 글 조회 | viewCount 증가 없음 |
| 거부 | 존재하지 않는 id | 999999 | 404 NOTICE_NOT_FOUND |
| 거부 | 타인의 비공개 초안 | 다른 사용자의 draft id | 404 NOTICE_NOT_FOUND (존재 노출 금지) |
| 거부 | 토큰 누락 | 헤더 없음 | 401 UNAUTHORIZED |

**규칙 요약**
- 비공개 항목(`published=false`)은 **작성자 본인에게만** 노출
- 타인의 비공개 항목 접근은 **404로 응답** (403 대신) — 존재 여부 자체를 감춤
- 조회수는 **작성자 본인이 아닐 때만** 증가

---

### 5-3. 공지사항 생성·수정·삭제

```
POST   /api/notices              # 생성
PUT    /api/notices/{noticeId}   # 수정
DELETE /api/notices/{noticeId}   # 삭제
Authorization: Bearer {JWT}
Role: ROLE_NOTICE_ADMIN (필수)
```

**Request (POST/PUT):**
```json
{
  "title": "시스템 점검 안내",
  "content": "## 점검 일정\n...",
  "pinned": true,
  "published": true
}
```

**Response (POST):** 201 Created + 생성된 공지 전체 (상세 조회와 동일 스키마)
**Response (PUT):** 200 OK + 수정된 공지 전체
**Response (DELETE):** 204 No Content

**처리 순서 (단일 트랜잭션):**
1. 사용자 권한 확인 (`ROLE_NOTICE_ADMIN`)
2. 입력 검증 (title 1~200자, content 1~50000자, pinned/published는 boolean)
3. POST: INSERT + `creator`, `created` 자동 기입
4. PUT: UPDATE + `modifier`, `modified` 자동 기입 (대상 없으면 404)
5. DELETE: DELETE (하드 삭제, 대상 없으면 404)

**에러 응답**

| 조건 | Status | 에러 코드 | 메시지 |
|---|---|---|---|
| 필수 필드 누락 (title/content) | 400 | INVALID_INPUT | "제목/내용은 필수입니다" |
| title 길이 초과 (200자 초과) | 400 | TITLE_TOO_LONG | "제목은 200자 이내여야 합니다" |
| content 길이 초과 (50000자 초과) | 400 | CONTENT_TOO_LONG | "내용은 50000자 이내여야 합니다" |
| 수정/삭제 대상 없음 | 404 | NOTICE_NOT_FOUND | "공지사항을 찾을 수 없습니다" |
| 권한 없음 (`ROLE_NOTICE_ADMIN` 아님) | 403 | FORBIDDEN | "공지 관리 권한이 필요합니다" |
| 토큰 누락/만료 | 401 | UNAUTHORIZED | "인증이 필요합니다" |
| DB 오류 | 500 | INTERNAL_ERROR | "일시적 오류입니다" |

**검증 시나리오**

| 구분 | 시나리오 | 입력 | 기대 결과 |
|---|---|---|---|
| 정상 | 공지 생성 | 유효 body + admin | 201 + 생성 결과 반환 |
| 정상 | 공지 수정 | 유효 body + admin + 존재 id | 200 + 수정 결과 반환 |
| 정상 | 공지 삭제 | admin + 존재 id | 204 |
| 정상 | 비공개 초안 생성 | `published=false` | 201 + 목록 조회에 노출 안 됨 |
| 에지 | title 정확히 200자 | 200자 길이 | 201 |
| 에지 | pinned 토글 | 수정 요청으로 pinned만 변경 | 200 + 목록 정렬 즉시 반영 |
| 거부 | title 누락 | `title` 없음 | 400 INVALID_INPUT |
| 거부 | title 201자 | 201자 | 400 TITLE_TOO_LONG |
| 거부 | content 50001자 | 50001자 | 400 CONTENT_TOO_LONG |
| 거부 | 일반 사용자가 생성 시도 | ROLE 없음 | 403 FORBIDDEN |
| 거부 | 없는 id 수정 | PUT /api/notices/999999 | 404 NOTICE_NOT_FOUND |

**규칙 요약**
- 생성/수정/삭제는 `ROLE_NOTICE_ADMIN` 권한자만 가능
- 생성/수정 시 `creator`/`modifier`는 JWT에서 추출한 사용자 ID로 **서버가 강제 설정** (클라이언트 값 무시)
- 삭제는 하드 삭제 (soft delete 미사용 — 감사 요구사항 없음)
- 입력 검증 실패 시 400, 권한 실패 시 403, 대상 없음 404 — 일관 유지

---

## 화면·기능 (§3·§4) — Frontend 컴포넌트 구조

### 화면 레이아웃 (ASCII 와이어프레임)

```
┌──────────────────────────────────────────────────────────────┐
│  [공지사항]                              [ + 새 공지 ] (admin) │
├──────────────────────────────────────────────────────────────┤
│  검색: [_____________________]  [ 검색 ]                      │
├──────────────────────────────────────────────────────────────┤
│  📌 [고정] 시스템 점검 안내                   admin01 · 04-15 │
│  📌 [고정] 서비스 이용약관 변경                admin02 · 04-10 │
│  ─────────────────────────────────────────────────────────   │
│  신규 기능 배포 안내                          admin01 · 04-14 │
│  장애 복구 완료 공지                          admin02 · 04-13 │
│  이벤트 당첨자 발표                           admin01 · 04-12 │
│  ...                                                          │
├──────────────────────────────────────────────────────────────┤
│                          [ < 1 2 3 > ]                        │
└──────────────────────────────────────────────────────────────┘
```

### 상세/편집 팝업 (ASCII 와이어프레임)

```
┌────────────────────────────────────────────┐
│  공지사항 작성                        [ ✕ ] │
├────────────────────────────────────────────┤
│  제목:  [________________________________] │
│  내용:  ┌──────────────────────────────┐   │
│         │                              │   │
│         │  (Markdown 편집기)            │   │
│         │                              │   │
│         └──────────────────────────────┘   │
│  [☑] 상단 고정                              │
│  [☑] 즉시 게시 (체크 해제 시 비공개 초안)   │
├────────────────────────────────────────────┤
│                         [ 취소 ]  [ 저장 ] │
└────────────────────────────────────────────┘
```

### 패널 동작

| 항목 | 값 |
|---|---|
| 방식 | 단일 컬럼 (사이드 패널 없음) |
| 모바일 대응 | 반응형 — 480px 이하에서 날짜 축약 표시 |
| 상태 저장 | 페이지 번호만 URL 쿼리로 유지 |

### 공지 목록

#### 검색/필터 툴바

| 영역 | 구성 | 설명 |
|---|---|---|
| 키워드 검색 | 텍스트 입력 + 검색 버튼 | 제목 부분 일치 |
| 페이지네이션 | 하단 숫자 버튼 | 10페이지 단위 노출 |

#### 아이콘 툴바 (관리자만)

| 순서 | 아이콘 | 기능 |
|---|---|---|
| 1 | `+` (새 공지) | 작성 팝업 열기 |
| 2 | `✏` (수정) | 목록 행 hover 시 표시 |
| 3 | `🗑` (삭제) | 확인 다이얼로그 후 삭제 |

#### 목록 그리드 컬럼

| 컬럼키 | 헤더 | 너비 | 매핑 |
|---|---|---|---|
| `pinned` | 📌 | 40px | `notice.pinned` (true면 아이콘 표시) |
| `title` | 제목 | auto | `notice.title` (클릭 시 상세) |
| `creator` | 작성자 | 120px | `notice.creator` |
| `created` | 작성일 | 100px | `notice.created` (MM-DD) |

#### 동작 흐름

1. 페이지 진입 → `GET /api/notices?page=0&size=20`
2. 행 클릭 → 상세 페이지(/notice/{id}) 이동 + `GET /api/notices/{id}`
3. 관리자가 "새 공지" 클릭 → 작성 팝업 열기
4. 저장 클릭 → `POST /api/notices` → 성공 시 팝업 닫고 목록 새로고침

### 작성/수정 팝업

**목적**
공지사항을 작성하거나 수정한다.

**트리거**
- 작성: 목록 상단 `+ 새 공지` 버튼 (관리자만 노출)
- 수정: 상세 페이지의 `수정` 버튼 (관리자만 노출)

**데이터 소스 (수정 시)**
`GET /api/notices/{id}` 로 기존 데이터 로드 후 폼에 채움

**처리 로직 (저장)**
- 작성: `POST /api/notices` (body: title/content/pinned/published)
- 수정: `PUT /api/notices/{id}` (body 동일)

**하단 버튼**

| 버튼 | 동작 |
|---|---|
| 취소 | 변경사항 있으면 확인 다이얼로그 → 팝업 닫기 |
| 저장 | 유효성 검증 통과 시 API 호출 |

**규칙 요약**
- 제목/내용 필수, 제목 최대 200자, 내용 최대 50000자
- `published=false` 로 저장 시 목록에 노출되지 않음 (작성자 본인만 접근 가능)
- `pinned` 토글 시 즉시 정렬 반영

**성공 후 Frontend 동작**
1. 팝업 닫기
2. 토스트 "저장되었습니다" 표시
3. 목록 새로고침 (`GET /api/notices`)

**사용자 시나리오**

| 구분 | 시나리오 | 사용자 액션 | 기대 화면 반응 |
|---|---|---|---|
| 정상 | 새 공지 작성 성공 | 제목/내용 입력 후 저장 | 팝업 닫힘 + 토스트 "저장되었습니다" + 목록 최신화 |
| 정상 | 수정 저장 성공 | 기존 공지 수정 후 저장 | 팝업 닫힘 + 상세 화면 내용 갱신 |
| 정상 | 초안 저장 | `즉시 게시` 해제 후 저장 | 본인 목록에만 "초안" 뱃지로 표시 |
| 에지 | 제목 200자 경계 | 정확히 200자 입력 | 저장 성공 |
| 에지 | 네트워크 지연 | 저장 클릭 후 응답 지연 | 저장 버튼 비활성 + 스피너 표시 |
| 거부 | 제목 빈 값 | 제목 비우고 저장 | 제목 필드 빨간 테두리 + "제목을 입력해주세요" |
| 거부 | 제목 201자 이상 | 초과 입력 | 입력 차단 또는 저장 시 에러 메시지 |
| 거부 | 권한 없음 | 관리자 아닌 사용자의 `PUT` | 팝업에 "공지 관리 권한이 필요합니다" 표시 |
| 거부 | 동시 수정 충돌 | 다른 관리자가 먼저 수정 | `[TODO]` 낙관적 락 도입 여부 추후 결정 |

**구현 파일**

| 파일 | 역할 |
|---|---|
| `frontend/src/features/notice/NoticeFormDialog.tsx` | 작성/수정 팝업 |
| `frontend/src/features/notice/noticeFormSchema.ts` | Zod 스키마 (title/content 검증) |

### 컴포넌트 목록

| 컴포넌트 | 파일 | 역할 |
|---|---|---|
| NoticeListPage | `frontend/src/features/notice/NoticeListPage.tsx` | 목록 페이지 루트 |
| NoticeDetailPage | `frontend/src/features/notice/NoticeDetailPage.tsx` | 상세 페이지 |
| NoticeList | `frontend/src/features/notice/NoticeList.tsx` | 목록 그리드 |
| NoticeSearchBar | `frontend/src/features/notice/NoticeSearchBar.tsx` | 검색 입력 |
| NoticeFormDialog | `frontend/src/features/notice/NoticeFormDialog.tsx` | 작성/수정 팝업 |
| NoticeDeleteConfirm | `frontend/src/features/notice/NoticeDeleteConfirm.tsx` | 삭제 확인 다이얼로그 |

### 키보드 네비게이션

| 키 | 조건 | 동작 |
|---|---|---|
| `Enter` | 검색 입력 포커스 | 검색 실행 |
| `Esc` | 팝업 열림 | 팝업 닫기 (변경사항 있으면 확인) |
| `Ctrl+S` | 팝업 열림 | 저장 |

### 성능 최적화

| 항목 | 설명 |
|---|---|
| 목록 페이징 | 한 페이지 최대 100건 상한으로 과도한 로드 차단 |
| 인덱스 활용 | `(published, created DESC)` 복합 인덱스로 목록 정렬 최적화 |
| 상세 조회 캐싱 | 클라이언트 캐시 30초 유지 (재요청 억제) |
