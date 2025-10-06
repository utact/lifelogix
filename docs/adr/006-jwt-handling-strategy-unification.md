# [ADR-006] JWT 처리 전략 통합 및 라이브러리 역할 분리

- **Status:** Adopted
- **Refines:** `003-authentication-strategy.md`
- **Last Updated:** 2025-10-06

---

## Context (컨텍스트)

Spring Boot 3, Spring Security 6 환경에서 JWT 기반의 인증/인가 시스템을 구축하는 과정에서 두 가지 심각한 문제에 직면했다.

1.  **지속적인 컴파일 오류:** `io.jsonwebtoken (jjwt)` 라이브러리를 사용하여 토큰을 파싱하는 `Jwts.parserBuilder()` 메서드에서 원인 불명의 컴파일 오류가 지속적으로 발생했다. `build.gradle` 의존성 설정은 정확했으며, 모든 종류의 캐시(Gradle, IntelliJ)를 삭제하고 프로젝트를 재설정해도 문제가 해결되지 않았다.
2.  **잠재적인 런타임 오류:** 의존성 트리를 분석한 결과, `spring-boot-starter-oauth2-resource-server`가 `com.nimbusds:nimbus-jose-jwt` 라이브러리를 Transitive Dependency로 포함하는 것을 발견했다. 이로 인해 프로젝트 내에 `jjwt`와 `nimbus-jose-jwt`라는 두 개의 JWT 라이브러리가 공존하게 되었다. 이 두 라이브러리에서 비밀 키를 처리하는 방식(Base64 인코딩 여부)이 달라, 컴파일 문제가 해결되더라도 런타임에서 토큰 서명 검증 실패가 예견되는 상황이었다.

이러한 기술적 불확실성과 충돌은 개발 생산성을 심각하게 저해하고 시스템의 안정성을 위협하는 주요 요인이었다.

---

## Decision (결정)

안정성과 프레임워크 호환성을 최우선으로 고려하여, 다음과 같이 JWT 처리 전략을 결정한다.

1.  **Spring Security 표준 채택:** 토큰 **검증(Validation) 및 파싱(Parsing)은** `spring-boot-starter-oauth2-resource-server`의 기본 구현체인 `Nimbus` 라이브러리 기반의 `JwtDecoder`를 사용한다. 이는 스프링 시큐리티의 표준 방식을 따르는 것으로, 프레임워크가 제공하는 강력하고 안정적인 보안 기능을 최대한 활용한다.
2.  **`JwtTokenProvider`의 역할 축소:** 직접 구현한 `JwtTokenProvider`의 역할은 토큰 **생성(Generation)으로 한정**한다. 문제가 발생한 `parserBuilder()`를 사용하는 검증 및 파싱 관련 코드를 모두 제거하여 컴파일 오류의 근본 원인을 제거한다.
3.  **비밀 키 처리 방식 통일:** 토큰을 생성하는 `JwtTokenProvider(jjwt)`와 검증하는 `SecurityConfig(Nimbus)` 간의 키 불일치 문제를 해결하기 위해, `SecurityConfig`의 `jwtDecoder` Bean이 비밀 키를 **Base64로 디코딩**하도록 수정한다. 이로써 서명과 검증에 사용되는 키를 완벽하게 일치시킨다.

---

## Consequences (결과)

### 긍정적
* **컴파일 오류 해결:** 문제가 되던 코드를 제거함으로써 개발을 가로막던 가장 큰 장애물을 해결했다.
* **런타임 안정성 확보:** 두 라이브러리 간의 논리적 충돌을 사전에 방지하여 런타임에서 발생할 수 있는 치명적인 인증 실패 오류를 예방했다.
* **코드 단순화 및 역할 분리:** `JwtTokenProvider`는 생성, `SecurityConfig`는 검증이라는 명확한 역할 분리가 이루어져 코드의 가독성과 유지보수성이 향상되었다.
* **프레임워크 활용 극대화:** 스프링 시큐리티의 내장된 기능을 활용함으로써 직접 구현해야 하는 보일러플레이트 코드를 줄이고, 더 안전하고 표준적인 아키텍처를 구축하게 되었다.

### 부정적 (고려사항)
* **두 개의 JWT 라이브러리 의존성:** 프로젝트 클래스패스에 `jjwt`와 `nimbus-jose-jwt`가 모두 존재하게 된다. 하지만 역할이 명확히 분리되었기 때문에 혼란의 여지는 최소화된다.