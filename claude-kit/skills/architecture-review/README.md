# Architecture Review Skill

> Macro-level analysis of Java project structure, packages, and dependency direction

## What It Does

Analyzes project architecture at a high level:
- Package organization (by-layer vs by-feature)
- Dependency direction between layers (Controller → Service → Mapper)
- Module boundaries and coupling
- Architectural anti-patterns (god packages, layer-boundary leaks, etc.)

> **Stack assumption (ILIS)**: Spring Boot 3.x, Java 21, **MyBatis** (no JPA), PostgreSQL, traditional layered MVC. Layer boundaries, one-way dependency (Controller → Service → Mapper), no cycles, and no god packages always apply.

## When to Use

- "Review the architecture of this project"
- "Is this package structure good?"
- "Check if the layer boundaries hold (Controller / Service / Mapper)"
- "Find architectural violations"
- Before major refactoring efforts

## Key Concepts

### Package Strategies

| Strategy | Best For | Trade-off |
|----------|----------|-----------|
| By-layer | Small projects, quick start | Scatters related code |
| By-feature | Medium projects, clear modules | Need shared kernel |

### Dependency Direction

```
Controller → Service → Mapper (data access)

Rule: Dependencies flow top-down only (Controller → Service → Mapper).
A Service must not import a Controller or web/HTTP types;
a Controller must not skip the Service to call a Mapper.
```

## Example Usage

```
You: Review the architecture of this project

Claude: [Analyzes package structure]
        [Checks dependency direction]
        [Identifies violations]
        [Provides prioritized recommendations]
```

## What It Checks

1. **Package Structure** - Organization, naming consistency
2. **Dependency Direction** - Top-down flow (Controller → Service → Mapper), no upward/backward leaks
3. **Layer Boundaries** - Proper separation of concerns
4. **Module Boundaries** - Clear APIs, encapsulation
5. **Scalability** - Could features be extracted?

## Related Skills

- `solid-principles` - Class-level design (this skill is package/module level)
- `design-patterns` - Implementation patterns (this skill is structural)
- `clean-code` - Code quality (this skill is architectural quality)

## References

- [Clean Architecture (Uncle Bob)](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) — 의존 방향 개념의 배경(참고용, ILIS는 전통 MVC)
- [Package by Feature](https://phauer.com/2020/package-by-feature/)
