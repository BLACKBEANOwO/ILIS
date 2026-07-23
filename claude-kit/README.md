# claude-kit — Spring Boot 3 · MyBatis · React 프로젝트용 Claude 자산

Spring Boot 3 · MyBatis · React 스택 프로젝트에서 쓰는 Claude Code 스킬·에이전트·커맨드 모음.
이 폴더를 압축해 전달하면 그대로 새 프로젝트에 적용할 수 있다.

**설치**는 프롬프트 하나로 한다. 프로젝트 루트에서 `claude-kit/SETUP.md 대로 이 프로젝트에 설치해줘` 를 던지면 된다 (절차는 [SETUP.md](SETUP.md)).
**팀 표준·규약·자산 사용법**은 [ILIS_개발환경_안내.html](ILIS_개발환경_안내.html) 을 연다 (브라우저).

## 이 kit 이 하는 일

**개발자가 누구든, 이 자산을 적용하면 명명·구조·계층 형태가 일정하게 나오도록 하는 것**이 목적이다.

- **현재 적용 대상은 ILIS 편집시스템**이다. 규약의 업무 전제(배정 기반 단계 진행 등)도 그 기준이다
- ILIS 사업은 **편집시스템·관리시스템·포털** 세 시스템으로 구성되고 각자 배포된다.
  다른 시스템에도 이 자산을 쓸 수 있게 하려면 **환경·시스템에 따라 달라지는 것을 규약에서 분리**해야 한다
- 그래서 이 kit 이 담는 것은 **환경이 달라져도 공통으로 쓸 수 있는 기술 선택과 가이드라인 구조**다

> **보장 범위**: 명명·패키지 구조·계층 책임·응답 형식·주석 형식까지다.
> 디자인·빌드 도구·base 패키지명이 미확정인 동안 **화면과 프로젝트 골격까지 같아지지는 않는다.**

### 규약에 담는 것 / 담지 않는 것

| | 예 | 기준 |
|---|---|---|
| **담는다** | 명명 규칙, 패키지·폴더 구조, 계층 책임, 응답 형식, 주석 태그 | 환경이 바뀌어도 **같다** |
| **담지 않는다** | 저장 매체, 루트 경로, 용량 제한, 허용 확장자, 서빙 방식, DB 접속 정보 | 환경·배포마다 **다르다** |
| **편집시스템 전제** | 배정 기반 단계 진행, 200+FAIL 닫힌 목록, JWT 인증 | 다른 시스템에 쓸 때 **재검토 대상** |

환경에 따라 달라지는 값은 **빼는 것이 아니라 꽂을 자리를 정해둔다.**
규약은 `${FILE_ROOT}` 처럼 주입 지점과 형식을 규정하고, 실제 값은
기능명세서(`docs/template.md`)와 환경별 설정에서 정한다.

> 환경 종속 값을 규약에 넣으면 다른 시스템에 적용할 수 없고,
> 반대로 형식까지 명세로 미루면 개발자마다 결과가 달라진다.

### 그래서 검증도 그 기준으로 한다

규약을 만든 사람이 스스로 대조하면 "모순이 없다"까지만 확인된다.
**규약 문서만 받은 다른 작업자가 같은 결과를 내는지**를 본다.

| 검증 | 방법 | 결과 |
|---|---|---|
| 백엔드 | 규약만 주고 판례 도메인 작성 → 21항목 대조 | 21 / 21 |
| 프론트 | 규약만 주고 판례 화면 작성 → 22항목 대조 | 22 / 22 |
| DB | 규약만 주고 DDL 작성 → **PostgreSQL 17 에 실제 실행** 후 카탈로그 대조 | 18 / 18 |

> **이 수치가 증명하는 것은 "규약이 말한 것은 지켜졌다"까지다.** 대조 항목이 규약에서 파생되므로,
> 규약에 없는 축(디자인·파일명 생성 규칙·오류 코드 작명)은 애초에 검사되지 않는다.

## 대상 스택

- **백엔드**: Spring Boot 3.x · JDK 17 이상 · **MyBatis**(SQL은 XML 매퍼) · REST · **PostgreSQL** · Logback
- **프론트**: React + TypeScript · Vite · React Router · shadcn/ui + Tailwind ·
  TanStack Query + Zustand · React Hook Form + Zod · react-i18next (SPA, nginx 정적 배포)
- **VCS/트래커**: GitLab

> 빌드 도구(Gradle/Maven)는 **미확정**이다 (확정 경로: 출장 담당자 협의).
> DBMS는 **PostgreSQL 을 작업 전제로 진행**한다 — 정식 확정은 출장 담당자 협의 결과를 따른다.

## 자산 종류란?

- **Skill** — 특정 작업에 대한 전문 지침·지식 묶음. 요청 문구가 맞으면 Claude가 자동 발동하거나 `/스킬명`으로 호출한다. (예: "코드 리뷰해줘" → 리뷰 스킬)
- **Agent** — 서브에이전트 역할 프롬프트. 작업을 위임하면 그 역할(리뷰어·구현자 등)로 동작한다.
- **Command** — `/mainline:이름` 으로 실행하는 슬래시 커맨드. 정해진 절차를 수행한다. (예: 커밋, MR 생성)
- **Context** — 프로젝트 배경 정보 파일(아키텍처·코딩표준·보안정책 등). 채워두면 Claude가 참고한다.
- **Template** — 산출물 양식(MR 요약·테스트 계획·장애 분석 등).
- **docs/template.md** — 기능명세서 작성용 질문 양식(feature-spec이 읽음).
- **CLAUDE.global.md** — 모든 프로젝트에 적용되는 전역 규정.
- **CLAUDE.backend.md** — 백엔드 프로젝트 규약. 저장소의 `backend/CLAUDE.md` 로 배치한다.
- **CLAUDE.frontend.md** — 프론트엔드 프로젝트 규약. 저장소의 `frontend/CLAUDE.md` 로 배치한다.
- **document-standards.md** — 보고용 산출물 작성 규정. CLAUDE.global.md가 `~/.claude/document-standards.md` 경로로 지시하므로 **함께 복사해야 한다**(경로 유지).
- **작업일지/** — 작업 이력을 남기는 방식(선택).

## 구성

| 폴더/파일 | 내용 | 적용 위치 |
|---|---|---|
| `SETUP.md` | 설치 런북 — 프롬프트 하나로 kit 을 프로젝트에 적용 | (Claude 가 읽고 실행) |
| `ILIS_개발환경_안내.html` | 팀 표준 문서 — 저장소 구조·규약·자산 사용법·온보딩 (정본) | 브라우저로 열람 |
| `skills/` (14) | 코드리뷰·패턴 11종 + 기능개발 워크플로(feature-spec/implement/test) 3종 | `.claude/skills/` |
| `agents/` (4) | code-reviewer · implementer · security-reviewer · test-runner | `.claude/agents/` |
| `commands/mainline/` (7) | commit · commit-split · fix-failing-test · review-risk · start-feature · implement-ticket · merge-request | `.claude/commands/` |
| `context/` (5) | architecture · coding-standards · security-policy · testing-policy · repo-map (채움용 스켈레톤) | `.claude/context/` |
| `templates/` (3) | incident-analysis · mr-summary · test-plan | `.claude/templates/` |
| `docs/template.md` | feature-spec 스킬이 읽는 기능명세서 §1~§10 정본 | 프로젝트 `docs/` |
| `CLAUDE.global.md` | 전역 규정(언어·작업일지 운영·기술선택·금지사항 등) | `~/.claude/CLAUDE.md` |
| `CLAUDE.backend.md` | 백엔드 규약(패키지·명명·계층·공통 응답 포맷·주석·금지사항) | 저장소 `backend/CLAUDE.md` |
| `CLAUDE.frontend.md` | 프론트엔드 규약(폴더·명명·API 응답 해체·서버/클라이언트 상태·폼·다국어) | 저장소 `frontend/CLAUDE.md` |
| `document-standards.md` | 산출물 작성 규정(용도 확인·미확정 표기·부연 표기·보고 서사). CLAUDE.global.md가 경로로 지시 | `~/.claude/document-standards.md` |
| `작업일지/` (선택) | LLM 위키 작업일지 스캐폴드 (GUIDELINES·template·INDEX) | 프로젝트 `작업일지/` |

### 스킬 14종

| 스킬 | 용도 |
|---|---|
| `spring-boot-patterns` | Controller·Service·MyBatis Mapper·DTO·예외·공통 응답 포맷·설정·테스트 작성 패턴 |
| `react-patterns` | 인터셉터·`BusinessError`·TanStack 훅·RHF+Zod·서버오류 매핑·날짜 포맷 코드 패턴 |
| `java-comment-style` | javadoc·주석 표준 (필수 태그 + 호출 그래프) |
| `shadcn-components` | shadcn/ui 컴포넌트 설치·배치·조합 규약, 사내 레지스트리 |
| `db-convention` | PostgreSQL 명명·컬럼순서·타입선정·DDL·인덱스·금지객체 |
| `java-code-review` | Java 코드 리뷰 |
| `api-contract-review` | API 계약 리뷰 |
| `architecture-review` | 계층 구조 리뷰 (MyBatis MVC 단방향) |
| `logging-patterns` | 로깅 패턴 |
| `concurrency-review` | 동시성 검토 |
| `test-quality` | 테스트 품질 |
| `feature-spec` / `feature-implement` / `feature-test` | 기능 개발 워크플로 3종 |

## 적용 전 준비

설치 후 **수동으로 채워야 하는 것**이 있다 (전체 항목·절차는 [SETUP.md](SETUP.md) §5).

- **`frontend-builder`·`backend-builder` 에이전트**를 프로젝트 스택(React · Spring Boot 3 + MyBatis)으로
  정의해야 `feature-implement` 워크플로가 동작한다. kit에 포함되어 있지 않다.
- **`settings.json`·`.mcp.json`·훅**은 kit에 없다. 프로젝트 세팅 시 빌드 명령·GitLab·DB 값을 확정해 새로 작성한다.
- **빌드 도구(Gradle/Maven)·DBMS**는 미확정이다. 확정 후 위 설정 값에 반영한다.
- GitLab 커맨드(`implement-ticket`·`merge-request`)는 `glab` CLI 또는 GitLab MCP가 필요하다.
- **codex 리뷰 게이트**: `feature-*`가 선택적으로 codex를 호출한다. 기본 모델이 계정 미지원이면 `codex exec -m gpt-5.4`로 대체한다.

## 변경 이력

- **2026-07-20** — 사내 개발가이드 v0.51 규약 반영. `CLAUDE.backend.md` 신설,
  `spring-boot-patterns`·`java-comment-style` 수정(Service 인터페이스 미사용 · `get`/`find` 구분 ·
  ApiResponse 공통 응답 포맷 · `global`/`domain` 패키지 2분할 · javadoc 필수 태그).
- **2026-07-20** — 프론트엔드 규약 신설. `CLAUDE.frontend.md`(Vite + React Router + shadcn/ui ·
  TanStack Query + Zustand · RHF + Zod · 응답 해체 인터셉터 · 전 화면 다국어),
  `shadcn-components`·`db-convention` 스킬 신설. DBMS는 PostgreSQL 작업 전제로 전환.
- **2026-07-14** — kit 최초 구축 (ai-pv 자산을 ILIS 스택으로 이식).

설치 절차는 [SETUP.md](SETUP.md), 자세한 사용법·규약은 [ILIS_개발환경_안내.html](ILIS_개발환경_안내.html) 참고.
