변경사항을 분석하고 적절한 커밋 메시지를 생성하여 git commit 한다.

작업 절차:
1. git status로 변경된 파일 목록을 확인한다.
2. git diff로 staged 및 unstaged 변경 내용을 확인한다.
3. git log --oneline -10으로 최근 커밋 메시지 스타일을 파악한다.
4. 변경 내용을 분석하여 커밋 메시지를 작성한다.
5. 관련 파일을 staging 한다 (.env, credentials 등 민감 파일 제외).
6. 작성한 커밋 메시지를 사용자에게 보여주고 확인을 받는다.
7. 확인 후 커밋을 실행한다.

커밋 메시지 규칙:
Conventional Commits 형식을 사용한다 (feat, fix, refactor, docs, test, chore).
- 제목은 50자 이내, 본문은 필요한 경우에만 추가한다.
- 한글로 작성한다.
- "무엇을 왜 변경했는지"에 초점을 맞춘다.

주의사항:
- 변경사항이 없으면 빈 커밋을 만들지 않는다.
- .env, credentials.json 등 민감 파일은 staging하지 않고 경고한다.
- git add -A 대신 변경된 파일을 명시적으로 staging 한다.
