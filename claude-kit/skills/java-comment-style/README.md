# Java Comment Style

> 백엔드 Java 클래스의 javadoc·인라인 주석을 호출 그래프 기반 일관 표준으로 작성한다.

## What It Does

Service / Controller / Mapper 인터페이스 / IT 테스트 클래스에 일관된 javadoc·주석 톤을 적용한다. **다른 개발자가 코드를 처음 봤을 때 호출 그래프와 실행 결과를 즉시 파악할 수 있도록** 한다.

핵심 결과물:
- 클래스/메서드/헬퍼에 대한 3가지 javadoc 템플릿
- 호출처(`호출처:`)·사용처(`사용처:`) 명시로 IDE에서 클릭 추적 가능
- 메서드 반환 자료구조의 형태 예시 — 본문 안 봐도 동작 파악
- PR 시점 컨텍스트(`#16-B`/`Phase 4`/`결함 1`/날짜) 제거

> **범위 주의**: MyBatis XML 매퍼의 SQL 주석은 본 스킬 대상이 아니다(XML은 javadoc이 없음). SQL 주석 규약은 별도 MyBatis 표준을 따른다.

## When to Use

- "주석 추가해줘" / "javadoc 작성해줘"
- "주석 일관성 정리"
- "주석 표준대로 다시 써줘"
- 신규 Service/Controller/Mapper 클래스 생성 직후 (자동 적용)
- 큰 리팩토링 후 — 메서드 시그니처/책임 변경 시 주석도 함께 갱신

## Key Concepts

### 3 카테고리 javadoc 템플릿

| 카테고리 | 필수 섹션 |
|---|---|
| 클래스 | 역할 / (활성 조건·위임) / 책임 범위 |
| public 메서드 | 역할 / `호출처:` / `흐름:` |
| private 헬퍼 | 의미 / (예시) / `사용처:` / (`내부 호출:`) |

### 4 원칙

1. **사실 기반** — 코드가 표현하지 못하는 *제약·의도·부작용*만
2. **호출 그래프** — 어디서 호출 + 어느 헬퍼 사용
3. **결과 예시** — 반환 자료구조·값의 형태
4. **외부 참조 금지** — PR 시점 컨텍스트는 git log·명세서·작업일지에 위임

## Example Usage

**상황**: 사용자가 새 Service 클래스 생성 후 "주석 추가해줘" 요청.

```
1. 안티패턴 grep으로 외부 참조 검출
2. 클래스 javadoc 작성 (역할 + 책임 범위)
3. public 메서드 — 호출처(Controller endpoint) + 흐름
4. private 헬퍼 — 사용처
5. 컴파일 확인 + 회귀 테스트
```

적용 후 **결과**:
- 메서드 hover 시 `호출처:` 가 보여 호출 흐름 파악 즉시
- `{@link}` 클릭으로 호출 그래프 추적 가능

## Related Skills

- `java-code-review` — 주석 외 일반 코드 리뷰
- `spring-boot-patterns` — Spring 컴포넌트 작성 패턴 (직교)
- `test-quality` — JUnit/AssertJ 테스트 작성 (IT 메서드 주석은 본 스킬과 일부 겹침)

## References

- CLAUDE.md `주석` 섹션 — 외부 참조 금지 원칙
