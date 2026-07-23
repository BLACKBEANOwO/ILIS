---
name: architecture-review
description: Analyze Java project architecture at macro level - package structure, module boundaries, dependency direction, and layering. Assumes a traditional layered MVC + MyBatis stack (Controller / Service / Mapper / DTO). Use when user asks "review architecture", "check structure", "package organization", or when evaluating layer boundaries and dependency direction.
---

# Architecture Review Skill

Analyze project structure at the macro level - packages, modules, layers, and boundaries.

## When to Use
- User asks "review the architecture" / "check project structure"
- Evaluating package organization
- Checking dependency direction between layers
- Identifying architectural violations
- Assessing layer separation (Controller / Service / Mapper / DTO)

> **Stack assumption (ILIS)**: Spring Boot 3.x, Java 21, **MyBatis** (SQL in XML mappers, no JPA), PostgreSQL, traditional layered MVC — Controller → Service → **Mapper**, with DTOs at the web boundary. The core ideas below (layer boundaries, one-way dependency, no cycles, no god packages) are stack-agnostic and always apply.

---

## Quick Reference: Architecture Smells

| Smell | Symptom | Impact |
|-------|---------|--------|
| Package-by-layer bloat | `service/` with 50+ classes | Hard to find related code |
| Upward layer dependency | Service imports Controller / web types | Layers coupled backwards |
| Circular dependencies | A → B → C → A | Untestable, fragile |
| God package | `util/` or `common/` growing | Dump for misplaced code |
| Leaky abstractions | Controller calls Mapper directly, or handles SQL | Layer boundaries violated |

---

## Package Organization Strategies

### Package-by-Layer (Traditional)

```
com.example.app/
├── controller/
│   ├── UserController.java
│   ├── OrderController.java
│   └── ProductController.java
├── service/
│   ├── UserService.java
│   ├── OrderService.java
│   └── ProductService.java
├── mapper/                     # MyBatis Mapper interfaces (+ XML in resources)
│   ├── UserMapper.java
│   ├── OrderMapper.java
│   └── ProductMapper.java
└── dto/
    ├── UserDto.java
    ├── OrderDto.java
    └── ProductDto.java
```

**Pros**: Familiar, simple for small projects
**Cons**: Scatters related code, doesn't scale, hard to extract modules

### Package-by-Feature (Recommended)

```
com.example.app/
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserMapper.java
│   └── UserDto.java
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   ├── OrderMapper.java
│   └── OrderDto.java
└── product/
    ├── ProductController.java
    ├── ProductService.java
    ├── ProductMapper.java
    └── ProductDto.java
```

**Pros**: Related code together, easy to extract, clear boundaries
**Cons**: May need shared kernel for cross-cutting concerns

---

## Dependency Direction Rules

### The Golden Rule

ILIS는 전통 레이어드 MVC이므로 의존은 **한 방향(위 → 아래)** 으로만 흐른다.

```
┌─────────────────────────────────────────┐
│   Controller (web · HTTP 처리)          │
├─────────────────────────────────────────┤
│   Service (비즈니스 로직 · 트랜잭션)     │
├─────────────────────────────────────────┤
│   Mapper (MyBatis · SQL은 XML) → DB     │
└─────────────────────────────────────────┘

의존 방향: Controller → Service → Mapper (한 방향).
아래 계층은 위 계층을 절대 알면 안 된다
 — Service는 Controller·웹/HTTP 타입을 import하지 않고,
   Mapper는 Service·Controller로 거슬러 올라가지 않는다.
```

### Violations to Flag

```java
// ❌ Service depends on the web layer (upward leak — Service reaching up to Controller/web)
package com.example.user.service;

import jakarta.servlet.http.HttpServletRequest;  // web type in Service!
import org.springframework.web.bind.annotation.RequestParam;

@Service
public class UserService {
    // Business logic entangled with HTTP concerns → hard to test/reuse
    public UserDto find(HttpServletRequest request) { ... }
}

// ❌ Controller talks to the Mapper directly, skipping the Service
package com.example.user.controller;

import com.example.user.mapper.UserMapper;  // layer skipped!

@RestController
public class UserController {
    private final UserMapper userMapper;  // should go through UserService
}

// ✅ Each layer depends only on the one directly below (Controller → Service → Mapper)
package com.example.user.service;

@Service
public class UserService {
    private final UserMapper userMapper;      // Service → Mapper: OK

    public UserDto find(Long id) {            // plain params, no web types
        return userMapper.selectById(id);
    }
}
```

---

## Architecture Review Checklist

### 1. Package Structure
- [ ] Clear organization strategy (by-layer or by-feature)
- [ ] Consistent naming across modules
- [ ] No `util/` or `common/` packages growing unbounded
- [ ] Feature packages are cohesive (related code together)

### 2. Dependency Direction
- [ ] Dependencies flow top-down: Controller → Service → Mapper (never the reverse)
- [ ] Services don't import Controllers or web/HTTP types
- [ ] No circular dependencies between packages
- [ ] Clear dependency hierarchy

### 3. Layer Boundaries
- [ ] Controllers don't contain business logic
- [ ] Services don't know about HTTP (no `HttpServletRequest`, no `@RequestParam` in service methods)
- [ ] Mappers (data access) don't leak into Controllers — access goes through Services
- [ ] DTOs used at boundaries; persistence/query models not exposed raw to the web layer

### 4. Module Boundaries
- [ ] Each module has clear public API
- [ ] Internal classes are package-private
- [ ] Cross-module calls go through a module's public entry point (e.g. its Service), not internal classes
- [ ] No "reaching across" modules for internals

### 5. Scalability Indicators
- [ ] Could extract a feature to separate service? (microservice-ready)
- [ ] Are boundaries enforced or just conventional?
- [ ] Does adding a feature require touching many packages?

---

## Common Anti-Patterns

### 1. The Big Ball of Mud

```
src/main/java/com/example/
└── app/
    ├── UserDto.java
    ├── UserController.java
    ├── UserService.java
    ├── UserMapper.java
    ├── OrderDto.java
    ├── OrderController.java
    ├── ... (100+ files in one package)
```

**Fix**: Introduce package structure (start with by-feature)

### 2. The Util Dumping Ground

```
util/
├── StringUtils.java
├── DateUtils.java
├── ValidationUtils.java
├── SecurityUtils.java
├── EmailUtils.java      # Should be in notification module
├── OrderCalculator.java # Should be in order module (Service)
└── UserHelper.java      # Should be in user module (Service)
```

**Fix**: Move domain logic to appropriate modules, keep only truly generic utils

### 3. Anemic Domain Model (context-dependent — not a default violation in MVC)

```java
// Domain object is just data
public class Order {
    private Long id;
    private List<OrderLine> lines;
    private BigDecimal total;
    // Only getters/setters, no behavior
}

// All logic in "service"
public class OrderService {
    public void addLine(Order order, Product product, int qty) { ... }
    public void calculateTotal(Order order) { ... }
    public void applyDiscount(Order order, Discount discount) { ... }
}
```

**Note**: In traditional layered MVC (ILIS), a data-holder DTO plus logic-in-Service is the **normal, accepted** style — flag it only if the Service has grown so large that behavior clearly belongs on a cohesive object. A rich domain model is a deliberate choice, not a default requirement.

### 4. Mixing DTO and Persistence/Query Models

```java
// One class used as web request/response DTO, MyBatis query result,
// and business object all at once
public class User {
    private Long id;
    private String password;      // must never reach the web response
    private String internalNote;  // query-only column, leaks to the client
    private String confirmPassword; // web-only field, meaningless to the DB
}
```

**Fix**: Keep a clear DTO at the web boundary and a separate model for data access. Don't reuse one bag of fields across the request, the query result, and the response — it leaks columns outward and drags web-only fields into queries. (With MyBatis, models are plain POJOs, so this is about role separation, not framework annotations.)

---

## Analysis Commands

When reviewing architecture, examine:

```bash
# Package structure overview
find src/main/java -type d | head -30

# Largest packages (potential god packages)
find src/main/java -name "*.java" | xargs dirname | sort | uniq -c | sort -rn | head -10

# Layer boundary leaks (dependencies pointing the wrong way)
# Service importing web/HTTP types:
grep -rn "import jakarta.servlet\|import org.springframework.web\|HttpServletRequest" src/main/java --include="*Service*.java"
# Controller reaching straight into the Mapper (skipping Service):
grep -rln "import .*\.mapper\." src/main/java --include="*Controller*.java"

# God package check: oversized util/common
find src/main/java -path "*util*" -o -path "*common*" | wc -l

# Find circular dependencies (look for bidirectional imports)
# Check if package A imports from B and B imports from A
```

---

## Recommendations Format

When reporting findings:

```markdown
## Architecture Review: [Project Name]

### Structure Assessment
- **Organization**: Package-by-layer / Package-by-feature
- **Clarity**: Clear / Mixed / Unclear

### Findings

| Severity | Issue | Location | Recommendation |
|----------|-------|----------|----------------|
| High | Controller calls Mapper directly | `user/UserController.java` | Route data access through the Service |
| Medium | God package | `util/` (23 classes) | Distribute to feature modules |
| Low | Inconsistent naming | `service/` vs `services/` | Standardize to `service/` |

### Dependency Analysis
[Describe dependency flow, violations found]

### Recommendations
1. [Highest priority fix]
2. [Second priority]
3. [Nice to have]
```

---

## Token Optimization

For large codebases:
1. Start with `find` to understand structure
2. Grep for layer-boundary leaks (Service → web types, Controller → Mapper) instead of reading every file
3. Sample 2-3 features for pattern analysis
4. Don't read every file - look for patterns
