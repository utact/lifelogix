# 백엔드 아키텍처 가이드

이 문서는 LifeLogix 백엔드 시스템의 일관성, 확장성, 유지보수성을 보장하기 위한 핵심 아키텍처 원칙과 구현 패턴을 정의합니다. 모든 백엔드 개발은 이 가이드를 따르는 것을 원칙으로 합니다.

---

## 1. 패키지 구조 원칙

프로젝트의 모든 패키지 구조는 **'도메인 중심 모듈형 아키텍처(Domain-Centric Modular Architecture)'를** 따릅니다.

이는 기능(Feature) 단위의 높은 응집도와 계층(Layer) 간의 명확한 책임 분리를 통해, 프로젝트의 장기적인 확장성과 유지보수성을 확보하는 것을 목표로 합니다.

상세한 계층 구조, 역할, 규칙, 의존성 원칙은 아래의 의사결정 문서를 참조해야 합니다.

-   **`ADR-008`: 도메인 중심 모듈형 패키지 구조 채택**
-   **`ADR-009`: API 계층 내부 패키지 구조 세분화**

---

## 2. 인증(Authentication) 철학

### 2.1. Stateless 원칙

우리 서버는 클라이언트의 상태를 세션 등에 저장하지 않는 **완전한 무상태(Stateless)를** 유지하는 것을 원칙으로 합니다.

모든 요청은 JWT(JSON Web Token)를 통해 그 자체로 인증에 필요한 모든 정보를 포함해야 합니다. 이는 서버의 확장성을 보장하고, 클라이언트와 서버 간의 의존성을 낮추기 위함입니다.

### 2.2. JWT Payload 규칙

-   **Subject (`sub`) 클레임**: **반드시 사용자의 불변하는 `ID`(Primary Key)를 사용합니다.**
    -   **사유**: 이메일 등 변경 가능한 값은 사용자가 정보를 수정했을 때 기존 토큰을 무효화시키는 문제를 야기합니다. 불변하는 ID를 사용하는 것이 시스템 안정성을 보장하는 표준 방식입니다.
-   **Private Claims**: 역할(Role) 등 인가(Authorization)에 필요한 추가 정보는 Private Claim에 담아 사용합니다.

### 2.3. Spring Security와 Principal 해석 (⭐)

이 원칙은 과거 테스트 과정에서 발견된 `ApplicationContext` 로딩 실패 및 `null` 주입 문제를 해결하고 재발을 방지하는 것을 목적으로 합니다.

-   **원칙**: **반드시 `JwtAuthenticationConverter`를 커스텀하여 사용해야 합니다.**
-   **구현 절차**:
    1.  `JwtTokenProvider`는 토큰 생성 시 `sub` 클레임에 `user.getId().toString()`을 담아야 합니다.
    2.  `SecurityConfig`는 `sub` 클레임을 읽어 `Long` 타입으로 변환하고, 이 `Long` 값을 Principal로 갖는 인증 객체를 생성하는 `JwtAuthenticationConverter` Bean을 등록해야 합니다.
    3.  `SecurityFilterChain`에는 반드시 위에서 정의한 커스텀 `JwtAuthenticationConverter`를 적용해야 합니다.

이 원칙을 통해, 모든 컨트롤러는 타입 변환에 대한 걱정 없이 `@AuthenticationPrincipal Long userId`를 사용하여 인증된 사용자의 ID를 안전하고 일관되게 주입받을 수 있습니다.

---

## 3. 테스트 코드 원칙

### 3.1. Assertion 라이브러리 사용법

-   **원칙**: 모든 검증(Assertion) 로직은 AssertJ 라이브러리의 `org.assertj.core.api.Assertions` 클래스를 표준 진입점(entry point)으로 사용합니다.
    -   **사유**: AssertJ는 `AssertionsForClassTypes` 등 여러 진입점을 제공하지만, `Assertions`가 모든 핵심 검증 메서드를 포함하는 대표 클래스입니다. 모든 테스트 코드에서 `Assertions` 클래스만 `static import`하여 사용하는 것으로 컨벤션을 통일하면, "이 메서드는 어느 클래스에서 가져와야 하는가?"라는 혼란을 방지하고 코드의 일관성을 극대화할 수 있습니다. 이는 AssertJ의 표준 사용법이기도 합니다.

---

## 4. 데이터 접근(Data Access) 원칙

### 4.1. N+1 문제와 Fetch Join

-   **원칙**: 연관된 엔티티를 함께 조회할 때는 **반드시 Fetch Join (`JOIN FETCH`)을 사용하여 N+1 문제를 예방**합니다.

    -   **N+1 문제란?**: 연관관계가 설정된 엔티티를 조회할 때, 첫 쿼리(1) 이후 연관된 엔티티의 수(N)만큼 추가적인 쿼리가 발생하는 성능 저하 문제입니다. 예를 들어, 10개의 `TimeBlock`을 조회한 후, 각 `TimeBlock`에 연결된 `Activity` 정보를 얻기 위해 10번의 추가 쿼리가 발생하는 상황입니다.

-   **해결책**: `JOIN FETCH`는 연관된 엔티티를 처음부터 함께 조회해오도록 JPA에게 지시하여, 단 한 번의 쿼리로 모든 데이터를 가져올 수 있게 합니다.

### 4.2. 복잡한 쿼리는 `@Query` 사용

-   **원칙**: 여러 엔티티를 조인하거나 복잡한 조건을 포함하는 쿼리는 Spring Data JPA의 긴 쿼리 메서드 이름 대신, **`@Query` 어노테이션을 사용하여 JPQL을 직접 작성**합니다.

    -   **사유**: `findByActivity_User_IdAndDate`와 같은 긴 메서드 이름은 가독성을 해치고, 잠재적인 오타로 인한 오류를 유발하기 쉽습니다. `@Query`를 사용하면 SQL과 유사한 JPQL로 쿼리의 의도를 명확하게 표현할 수 있어 유지보수성이 크게 향상됩니다.

-   **구현 예시 (`TimeBlockRepository`)**:
    ```java
    @Query("SELECT tb FROM TimeBlock tb JOIN FETCH tb.activity a JOIN FETCH a.category c WHERE a.user.id = :userId AND tb.date = :date")
    List<TimeBlock> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    ```