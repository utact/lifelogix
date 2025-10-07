# [ADR-012] ErrorCode 열거형 기반 중앙화된 예외 처리 전략 채택

- **Status:** Adopted
- **Last Updated:** 2025-10-07

---

## Context (컨텍스트)

프로젝트 초기에는 `DuplicateEmailException`과 같이, 특정 예외 상황마다 별도의 커스텀 예외 클래스를 생성하는 방식을 사용했다. 그러나 `UserNotFound`, `CategoryNotFound`, `PermissionDenied` 등 다양한 비즈니스 예외 상황이 추가됨에 따라, 이러한 방식은 다음과 같은 문제점을 야기할 것으로 예상되었다.

-   **예외 클래스의 폭발적인 증가**: 사소한 예외 하나마다 새로운 클래스 파일을 생성해야 하므로 보일러플레이트 코드가 증가하고 클래스 관리가 복잡해진다.
-   **일관성 부재**: 각 예외 클래스마다 에러 메시지와 상태 코드를 개별적으로 관리하게 되어, 전체적인 에러 응답의 일관성을 유지하기 어렵다.

따라서 확장 가능하고 일관된 예외 처리 아키텍처 수립이 필요해졌다.

---

## Decision (결정)

모든 비즈니스 예외를 중앙에서 관리하기 위해, **`ErrorCode` 열거형(Enum)과 단일 `BusinessException` 클래스를 사용하는 전략**을 채택한다.

1.  **`ErrorCode.java` (Enum)**:
    -   애플리케이션 내에서 발생할 수 있는 모든 예측 가능한 비즈니스 예외를 열거형 상수로 정의한다.
    -   각 상수(`USER_NOT_FOUND` 등)는 대응하는 `HttpStatus`와 기본 에러 메시지를 멤버로 가진다.
    -   이 Enum이 프로젝트의 모든 에러 코드에 대한 **'단일 진실 공급원(Single Source of Truth)'** 역할을 한다.

2.  **`BusinessException.java` (Class)**:
    -   모든 비즈니스 예외를 표현하는 단일 `RuntimeException` 클래스이다.
    -   생성 시 `ErrorCode`를 전달받아, 해당 에러에 대한 상태 코드와 메시지 정보를 갖게 된다.

3.  **`GlobalExceptionHandler.java`**:
    -   `@RestControllerAdvice`를 통해 `BusinessException` 하나만 처리하도록 핸들러를 단순화한다.
    -   핸들러는 발생한 `BusinessException`으로부터 `ErrorCode`를 꺼내, 사전에 정의된 상태 코드와 메시지를 사용하여 일관된 `ErrorResponse`를 생성한다.

---

## Consequences (결과)

### 긍정적
-   **중앙화된 관리**: 모든 비즈니스 에러의 종류, 상태 코드, 메시지가 `ErrorCode` Enum 한 파일에 모여있어 파악 및 관리가 매우 용이하다.
-   **보일러플레이트 감소**: 새로운 예외 상황이 발생해도 별도의 클래스 파일 생성 없이 `ErrorCode`에 한 줄만 추가하면 되므로, 코드가 간결해지고 생산성이 향상된다.
-   **일관성 보장**: 모든 비즈니스 예외가 `GlobalExceptionHandler`의 단일 창구를 통해 처리되므로, API 에러 응답의 형식이 항상 일관되게 유지된다.

### 부정적
-   기존에 개별적으로 만들었던 커스텀 예외 클래스(`DuplicateEmailException` 등)들은 모두 폐기되고, 해당 로직은 `BusinessException`을 사용하도록 리팩토링되어야 한다.