# [ADR-013] 인증된 API를 위한 테스트 전략 수립

- **Status:** Adopted
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

인증된 API의 안정적인 테스트를 위해, 외부 의존성을 제거한 테스트용 보안 설정(`TestSecurityConfig`)과 인증된 사용자를 모의하는 커스텀 어노테이션(`@WithMockCustomUser`)을 조합하여 사용한다.

---

## Context (컨텍스트)

OAuth2 도입 후, 기존 테스트 코드가 새로운 인증 환경과 호환되지 않아 대부분 실패했다. 특히 `@WebMvcTest`에서 실제 `SecurityConfig`를 로드하며 발생하는 외부 의존성 문제로 인해, 안정적인 테스트 전략 수립이 시급해졌다.

---

## Decision (결정)

인증된 API를 위한 테스트 전략으로, **테스트용 보안 설정(`TestSecurityConfig`)과 커스텀 어노테이션(`@WithMockCustomUser`)을 조합하여 사용하는 방식**을 채택한다.

1.  **`TestSecurityConfig.java`**:
    -   `@TestConfiguration`을 사용하여 테스트 환경에서만 로드되는 별도의 보안 설정을 정의한다.
    -   실제 OAuth2 관련 빈(`CustomOAuth2UserService` 등)들은 `@MockBean`으로 대체하여 외부 의존성을 제거한다.
    -   `@WebMvcTest`에서 `SecurityConfig` 대신 이 `TestSecurityConfig`를 `@Import`하여 사용한다.

2.  **`@WithMockCustomUser` (Annotation)**:
    -   `@WithSecurityContext`를 사용하는 커스텀 어노테이션을 만들어, 테스트 코드에서 인증된 사용자를 쉽게 시뮬레이션할 수 있도록 한다.
    -   이 어노테이션은 `PrincipalDetails` 객체를 생성하여 Spring Security의 `SecurityContext`에 주입하는 역할을 한다. 이를 통해 컨트롤러에서 `@AuthenticationPrincipal`을 사용하여 인증된 사용자 정보를 직접 타입-세이프하게 받을 수 있다.

3.  **컨트롤러 리팩토링**:
    -   기존에 `Principal` 객체의 `getName()`을 호출하여 사용자 ID를 파싱하던 방식에서, `@AuthenticationPrincipal PrincipalDetails`를 직접 파라미터로 받도록 수정한다. 이는 테스트 용이성을 높일 뿐만 아니라, 런타임에 발생할 수 있는 타입 변환 오류를 컴파일 타임에 방지하는 더 안전한 방법이다.

---

## Consequences (결과)

### 긍정적
-   **테스트 안정성 확보**: 외부 환경이나 실제 인증 과정에 의존하지 않으므로, 네트워크 문제나 외부 서비스의 상태와 무관하게 항상 안정적인 테스트 실행이 가능하다.
-   **테스트 단순성 및 가독성 향상**: `@WithMockCustomUser` 어노테이션 하나만으로 인증된 사용자를 시뮬레이션할 수 있어, 테스트 코드가 훨씬 간결해지고 의도가 명확해진다.
-   **유지보수 용이성**: 실제 인증 로직과 테스트 인증 로직이 분리되어, 향후 인증 방식이 변경되더라도 테스트 코드에 미치는 영향을 최소화할 수 있다.
-   **타입-세이프한 컨트롤러**: 컨트롤러가 `PrincipalDetails` 타입을 직접 사용하게 되어, 더 안전하고 명확한 코드를 작성할 수 있게 되었다.

### 부정적
-   기존에 `@WithMockUser`를 사용하던 모든 컨트롤러 테스트 코드는 새로운 `@WithMockCustomUser` 어노테이션을 사용하도록 수정이 필요하다.
-   테스트를 위한 커스텀 어노테이션과 팩토리 클래스(`WithMockCustomUserSecurityContextFactory`)를 추가로 유지보수해야 하는 부담이 생긴다.
