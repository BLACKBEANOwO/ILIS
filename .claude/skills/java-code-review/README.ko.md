# Java Code Review (한글 번역본)

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

**Load**: `view .claude/skills/java-code-review/SKILL.md`

---

## 설명

Java 프로젝트를 위한 체계적 코드 리뷰 체크리스트. null 안전성, 예외 처리, 컬렉션, 동시성, 관용구, 리소스 관리, API 설계, 성능을 다룹니다.

---

## 사용 사례

- "이 클래스 리뷰해줘"
- "이 PR 문제점 봐줘"
- "PluginManager의 변경사항 코드 리뷰"
- "이 코드의 문제는 뭐야?"

---

## 예시

```
> view .claude/skills/java-code-review/SKILL.md
> "src/main/java/org/example/UserService.java 변경사항 리뷰해줘"
→ 심각도(Critical → Minor)별로 묶인 발견 사항 반환
```

---

## 체크리스트 카테고리

1. **Null 안전성** — NPE 위험, Optional 사용
2. **예외 처리** — 삼킨 예외, 스택트레이스
3. **컬렉션 & 스트림** — 순회, 가변성
4. **동시성** — 스레드 안전성, 경합 조건
5. **Java 관용구** — equals/hashCode, builder
6. **리소스 관리** — try-with-resources
7. **API 설계** — boolean 매개변수, 검증
8. **성능** — 문자열 연결, N+1 쿼리

---

## 메모 / 팁

- 집중된 변경 (단일 클래스 또는 PR)에 가장 효과적
- 좋은 관행에 대한 긍정 피드백 섹션 포함
- 리뷰 중 발견된 엣지 케이스에 대해 테스트 제안
