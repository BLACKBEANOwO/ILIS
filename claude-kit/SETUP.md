# SETUP — claude-kit 설치 런북

> **이 파일은 Claude Code 가 읽고 그대로 실행하는 설치 절차다.** 사람이 직접 명령을 치는 게 아니라,
> 프로젝트 루트에서 아래 한 프롬프트를 던지면 Claude 가 이 절차를 따라 kit 을 설치한다.
>
> ```
> claude-kit/SETUP.md 대로 이 프로젝트에 설치해줘
> ```

---

## Claude 에게 주는 지시

아래 순서대로 수행하라. **경로를 가정하지 말고 실제 환경을 먼저 확인**하고, **덮어쓰기 전에는 반드시 확인**하며,
실패하면 무작정 재시도하지 말고 원인을 보고하라. 각 단계 끝에 무엇을 했는지 한 줄로 남겨라.

### 0. 경로 확인 (가정 금지)

먼저 다음을 실제로 확인하고, 애매하면 사용자에게 묻는다.

- **프로젝트 루트** — 지금 작업 디렉터리가 대상 프로젝트의 루트가 맞는지. (`.git`·기존 소스 폴더로 판단)
- **kit 위치** — 압축을 푼 `claude-kit/` 폴더의 실제 경로. (이 SETUP.md 가 있는 폴더가 kit 루트다)
- **홈 Claude 경로** — 전역 규정을 둘 `~/.claude/` 의 실제 경로 (`echo $HOME` 로 확인). 없으면 만든다.
- **백엔드/프론트 폴더** — 이 프로젝트에 `backend/`·`frontend/` 가 있는지, 아니면 다른 이름인지. 없으면 규약 배치 위치를 사용자에게 확인.

> 아래 예시 명령의 `$KIT`·`$ROOT` 는 위에서 확인한 실제 경로로 바꿔 쓴다. 고정 경로를 그대로 복사하지 않는다.

### 1. `.claude/` 자산 복사 (필수)

프로젝트 루트에 `.claude/` 와 `docs/` 를 만들고 kit 자산을 복사한다.

- `skills/` → `.claude/skills/` (스킬 14종)
- `agents/` → `.claude/agents/` (에이전트 4종)
- `commands/` → `.claude/commands/` (커맨드 7종, `/mainline:*`)
- `context/` → `.claude/context/` (컨텍스트 스켈레톤 5종 — 프로젝트 사실로 채우는 용도)
- `templates/` → `.claude/templates/` (템플릿 3종)
- `docs/template.md` → 프로젝트 `docs/template.md` (feature-spec 이 읽는 기능명세서 정본)

기존에 같은 파일이 있으면 **덮어쓰기 전에 차이를 보여주고 확인**받는다.

### 2. 규약 CLAUDE.md 배치 (필수)

- `CLAUDE.backend.md` → 프로젝트 `backend/CLAUDE.md`
- `CLAUDE.frontend.md` → 프로젝트 `frontend/CLAUDE.md`

폴더명이 다르면(0단계에서 확인한 실제 이름) 그 위치에 둔다. 대상 폴더가 아직 없으면 사용자에게 만들지 물어본다.

### 3. 전역 규정 배치 (필수) — 덮어쓰기 금지

- `CLAUDE.global.md` → `~/.claude/CLAUDE.md`
- `document-standards.md` → `~/.claude/document-standards.md` **(반드시 같이. `CLAUDE.md` 가 이 경로를 지시한다)**

> **`~/.claude/CLAUDE.md` 가 이미 있으면 덮지 않는다.** 내용을 비교해 보여주고, 병합할지·유지할지 사용자에게 확인받는다.
> 같은 머신에서 이미 이 전역 규정을 쓰고 있으면 이 단계는 건너뛴다(다른 환경 이식·백업용으로만 kit 에 포함됨).
> `@document-standards.md` 같은 import 문법은 쓰지 않는다(프로젝트마다 승인 대화가 떠 수신자 선택에 좌우됨). 경로 지시 방식을 유지한다.

### 4. 작업일지 스캐폴드 (선택)

사용자가 원하면:

- `작업일지/` → 프로젝트 `작업일지/` (빈 LLM 위키 골격: GUIDELINES·template·INDEX·wiki/INDEX)
- `.gitignore` 에 `작업일지/` 추가 (개인 기록 → git 추적 제외)

### 5. 설치 후 리포트 (필수) — 수동으로 채워야 하는 것

복사만으로 끝나지 않는다. 아래를 **사용자에게 명확히 안내**하라. 이걸 빠뜨리면 워크플로가 동작하지 않는다.

- **builder 에이전트 정의 필요** — `feature-implement` 워크플로가 `frontend-builder`·`backend-builder` 에이전트에 위임한다.
  kit 에 없으므로 이 프로젝트 스택(React 프론트 · Spring Boot 3 + MyBatis 백엔드)으로 `.claude/agents/` 에 새로 정의해야 동작한다.
- **`settings.json`·`.mcp.json`·훅** — kit 에 없다. 프로젝트 세팅 시 빌드 명령·GitLab·DB 값을 확정해 새로 작성한다.
- **빌드 도구(Gradle/Maven)·DBMS** — 미확정. 확정 후 위 설정 값에 반영한다.
- **GitLab 커맨드** — `implement-ticket`·`merge-request` 는 `glab` CLI 또는 GitLab MCP 가 있어야 한다.
- **codex 리뷰 게이트** — `feature-*` 가 선택적으로 codex 를 호출한다. 기본 codex 모델이 계정 미지원이면 `codex exec -m gpt-5.4` 로 대체한다.

### 6. 검증 (필수)

마지막에 실제로 복사된 것을 나열해 확인한다.

- `.claude/` 에 `skills`(14) · `agents`(4) · `commands/mainline`(7) · `context`(5) · `templates`(3) 존재 및 개수
  (커맨드는 `commands/mainline/` 하위 7개다. `.claude/commands/` 직하위 개수가 아니다)
- `backend/CLAUDE.md`·`frontend/CLAUDE.md`·`docs/template.md` 존재
- (3단계 수행 시) `~/.claude/CLAUDE.md`·`~/.claude/document-standards.md` 존재
- 빠진 게 있으면 사용자에게 보고한다.

---

## 설치 후 무엇을 읽나

- 팀 표준·규약·자산 사용법 전체는 **[ILIS_개발환경_안내.html](ILIS_개발환경_안내.html)** (브라우저로 연다).
- kit 개요·구성은 [README.md](README.md).

> 이 두 문서는 `.claude/` 로 복사되지 않고 kit 안에 있다. **설치 후에도 `claude-kit/` 폴더를 지우지 말고 보존한다**
> (표준 문서·README 를 계속 참조하기 위함).
