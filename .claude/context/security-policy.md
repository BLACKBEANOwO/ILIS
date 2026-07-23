# Security Policy

- 비밀정보는 코드와 설정 파일에 직접 저장하지 않는다.
- 운영 자격 증명은 환경 변수나 사내 비밀관리 시스템으로 주입한다.
- 권한 변경, 외부 통신, 파일 시스템 변경은 리뷰 우선순위를 높인다.

## 인증 방식 — 세션 (이 프로젝트 = 관리시스템)

**이 저장소(ILIS 관리시스템)는 세션 방식이다. JWT를 쓰지 않는다.**

- 인증 후 상태는 **서버 세션(JSESSIONID 쿠키)** 으로 유지한다.
- 백엔드: Spring Security. 보안 설정은 `global/security/SecurityConfig`(패키지 `security/jwt` 아님).
- 프론트: `Authorization: Bearer` 토큰을 쓰지 않고, axios `withCredentials: true` 로 세션 쿠키를 전송한다.
  authStore는 토큰이 아니라 **로그인 사용자 정보(AuthUser)** 를 담는다.
- **인가(권한 판정)는 방식과 무관하게 유지** — 역할/데이터 기준 모두 Security 계층에서 `@PreAuthorize`로 처리, 403. (backend/CLAUDE.md §권한 판정)

### kit 규약(JWT)과의 차이 — 주의
kit의 `backend/CLAUDE.md`·`frontend/CLAUDE.md`·`react-patterns` 스킬은 **편집시스템 전제라 JWT 기준**으로 서술돼 있다.
이 관리시스템은 세션 방식이므로, 그 문서의 "JWT 첨부 / Bearer / 토큰 보관" 부분은 **이 파일 기준으로 대체**해 읽는다.
(README도 "JWT 인증"을 편집시스템 전제 = 다른 시스템에 쓸 때 재검토 대상으로 명시)

### 향후 SSO
편집시스템(JWT)과 관리시스템(세션)은 **각자 배포**되며, SSO는 둘 다 **동일 IdP·동일 프로토콜(OIDC 또는 SAML)** 의 클라이언트로 붙는 방식으로 통합한다.
로그인 후 유지 방식(세션 vs JWT)은 앱마다 독립이므로 혼용에 문제 없다. 단일 로그아웃(SLO)·세션/토큰 수명 정합만 도입 시 설계한다.

### 현재 상태 (초기 골격)
- `SecurityConfig`는 posture(세션·메서드 시큐리티)만 잡아둔 상태. **실제 로그인 엔드포인트·사용자 조회·CSRF·SSO는 미구현**(TODO).
- 현재 모든 경로 `permitAll` — 로그인/SSO 도입 시 `anyRequest().authenticated()` 로 전환.
- 세션 저장소는 인메모리(단일 인스턴스). 스케일아웃 시 Redis 등 공유 저장소로 전환.
