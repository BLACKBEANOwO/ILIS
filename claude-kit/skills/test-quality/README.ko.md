# Test Quality (JUnit 5 + AssertJ) — 한글 번역본

> **이 파일은 사람 참고용 번역본입니다.** Claude는 이 파일을 읽지 않고 영문 원본(`README.md` / `SKILL.md`)을 사용합니다.

**Load**: `view .claude/skills/test-quality/SKILL.md`

---

## 설명

Claude가 Java 프로젝트에 의미 있는 JUnit 테스트를 제안하고 테스트 커버리지를 개선하도록 돕습니다.

---

## 사용 사례

- "PluginManager.loadAll()에 테스트 추가해줘"
- "PluginLoaderTest의 기존 테스트 리뷰해줘"
- "lifecycle 모듈의 테스트 커버리지 개선해줘"

---

## 예시

```
> view .claude/skills/test-quality/SKILL.md
> "ExtensionFactory에 엣지 케이스 포함한 단위 테스트 추가해줘"
→ AssertJ assertion이 포함된 JUnit 5 테스트 생성
```

---

## 메모 / 팁

- 클래스/메서드 시그니처가 있을 때 가장 효과적
- 누락된 엣지 케이스나 null 체크 제안 가능
