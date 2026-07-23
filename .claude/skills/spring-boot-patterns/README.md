# Spring Boot Patterns (MyBatis)

> Spring Boot 3 + **MyBatis** 백엔드의 컨트롤러·서비스·Mapper·DTO·예외처리·설정·테스트 모범 패턴.

원본 ai-pv `spring-boot-patterns`(JPA 기반)에서 **Repository/JPA 패턴을 MyBatis Mapper(인터페이스 + XML) 패턴으로 교체**한 발췌본이다.

## 원본 대비 변경점

| 구분 | 원본(JPA) | 본 발췌본(MyBatis) |
|---|---|---|
| 데이터 접근 | `interface extends JpaRepository`, derived query, `@Query` JPQL | `@Mapper` 인터페이스 + XML SQL, `#{}`/`${}`, `<if>`/`<foreach>` |
| 없는 단건 | `Optional<T>` | `null` 반환 → 서비스에서 검사 |
| 쓰기 반환 | 저장 엔티티 | 영향 행 수(int) |
| 설정 | `spring.jpa.hibernate.ddl-auto` | `mybatis.*`, 스키마는 Flyway |
| 네이밍 | `UserMapper`=MapStruct | `UserMapper`=MyBatis, `UserConverter`=MapStruct (충돌 회피) |
| 유지 | Controller/DTO/예외/설정/테스트 패턴은 스택 무관하게 그대로 |

## 검증

- codex(gpt-5.4) 리뷰 실시 → 지적 반영: Mapper 인터페이스↔XML statement 정합(7:7), User 필드(name/age) resultMap·INSERT 반영, Service `update()` 구현 + `UpdateUserRequest` 추가, `@MapperScan` 문구 정정.
- MyBatis 문법(`#{}` vs `${}`, `@Param`, `useGeneratedKeys`, `resultMap`, `@MybatisTest`)은 리뷰에서 오류 없음 확인.

## When to Use

- "컨트롤러 만들어" / "서비스 추가" / "Mapper 작성" / "Spring Boot 도와줘"
- Spring Boot 코드 리뷰, 신규 프로젝트 구조 세팅

## Related Skills

- `java-code-review` — 일반 코드 리뷰
- `java-comment-style` — Mapper 인터페이스/Service javadoc 주석 표준
- `test-quality` — JUnit5/AssertJ 테스트 작성
