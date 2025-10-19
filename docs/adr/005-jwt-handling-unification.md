# [ADR-005] JWT 처리 라이브러리 역할 분리 및 표준화

- **Status:** Adopted
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

JWT 처리의 안정성을 높이기 위해, 토큰 검증은 Spring Security 표준(`Nimbus`)을 따르고, 토큰 생성은 `jjwt` 라이브러리를 사용하도록 역할을 명확히 분리한다.

---

## Context (컨텍스트)

`io.jsonwebtoken(jjwt)`와 Spring Security의 `nimbus-jose-jwt` 라이브러리가 공존하며 라이브러리 충돌 및 컴파일 오류가 발생했다. 안정적인 JWT 처리를 위해 라이브러리 간 역할을 명확히 정의하고 표준화할 필요가 생겼다.

---

## Decision (결정)

**JWT 처리의 책임을 명확히 분리하고, Spring Security의 표준 방식을 최대한 활용하는 전략을 채택한다.**

1.  **토큰 검증은 Spring Security 표준 사용:**
    -   토큰의 서명 검증 및 파싱은 `spring-boot-starter-oauth2-resource-server`가 기본으로 제공하는 `Nimbus` 기반의 `JwtDecoder`를 사용한다.
    -   이를 통해 프레임워크가 제공하는 안정적인 보안 기능을 활용하고, 라이브러리 충돌 가능성을 원천적으로 차단한다.

2.  **토큰 생성은 `jjwt` 라이브러리 사용:**
    -   직접 구현한 `JwtTokenProvider`는 토큰의 **생성(Generation)** 책임만 갖도록 역할을 축소한다.
    -   문제가 발생했던 토큰 검증 및 파싱 관련 코드를 `JwtTokenProvider`에서 모두 제거하여, 컴파일 오류의 원인을 제거하고 클래스의 책임을 단일화한다.

---

## Consequences (결과)

### 긍정적
- **안정성 및 호환성 확보:** Spring Security의 표준 JWT 처리 방식을 따름으로써, 향후 프레임워크 업그레이드 시 발생할 수 있는 호환성 문제를 최소화하고 런타임 안정성을 확보했다.
- **코드 단순화 및 명확한 역할 분리:** `JwtTokenProvider`는 생성, `SecurityConfig`는 검증이라는 역할이 명확히 분리되어 코드의 가독성과 유지보수성이 향상되었다.
- **개발 생산성 향상:** 지속적인 컴파일 오류를 해결하여 개발 과정의 불확실성을 제거하고 생산성을 높였다.

### 부정적
- **두 개의 JWT 라이브러리 의존:** 프로젝트 클래스패스에 `jjwt`와 `nimbus-jose-jwt` 두 개의 라이브러리가 모두 존재하게 된다. 하지만 역할이 명확히 분리되었기 때문에 실제 운영 환경에서 혼란을 야기할 가능성은 낮다.
