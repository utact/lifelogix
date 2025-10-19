# [ADR-004] 어노테이션 기반의 전역 예외 처리 전략 채택

- **Status:** Superseded by [ADR-012](../012-centralized-exception-handling-with-errorcode.md)
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

`@RestControllerAdvice`와 커스텀 예외 클래스에 `@ResponseStatus`를 붙여, 예외 종류가 추가되어도 핸들러 수정이 필요 없는 동적 예외 처리 전략을 채택한다.

---

## Context (컨텍스트)

다양한 비즈니스 예외를 일관된 형식의 에러 응답으로 변환하는 표준화된 방법이 필요하다. 예외마다 핸들러 메서드를 개별 작성하는 방식은 핸들러 클래스의 복잡도를 높이고 코드 중복을 야기할 수 있다.

---

## Decision (결정)

**`@RestControllerAdvice`와 `@ResponseStatus` 어노테이션을 조합한 동적 예외 처리 전략을 채택한다.**

1.  **커스텀 예외 정의:**
    -   각 비즈니스 예외 상황에 맞는 커스텀 예외 클래스(예: `UserNotFoundException`)를 정의한다.
    -   각 예외 클래스에 `@ResponseStatus` 어노테이션을 사용하여, 해당 예외가 발생했을 때 반환해야 할 `HttpStatus`를 선언한다. (예: `@ResponseStatus(HttpStatus.NOT_FOUND)`)

2.  **전역 예외 핸들러 구현:**
    -   `@RestControllerAdvice`가 선언된 `GlobalExceptionHandler` 클래스에, 최상위 비즈니스 예외(`BusinessException`)를 처리하는 단일 핸들러 메서드를 구현한다.
    -   핸들러는 `AnnotationUtils.findAnnotation()`을 사용하여 발생한 예외 클래스에 선언된 `@ResponseStatus`를 동적으로 찾아, 해당 상태 코드를 에러 응답에 반영한다.

---

## Consequences (결과)

### 긍정적
- **확장성 및 유지보수성:** 새로운 종류의 비즈니스 예외가 추가되더라도, 예외 클래스와 `@ResponseStatus` 어노테이션만 정의하면 되므로 전역 핸들러의 수정이 전혀 필요 없다. 이는 유지보수 포인트를 단일화하고 확장성을 높인다.
- **DRY 원칙 준수:** 예외와 HTTP 상태 코드의 매핑 정보가 해당 예외 클래스 내에 캡슐화되어, 코드 중복이 사라지고 응집도가 높아진다.
- **가독성:** 예외 클래스 선언부만 봐도 해당 예외가 어떤 HTTP 상태 코드를 유발하는지 명확히 알 수 있다.

### 부정적
- **암묵적인 동작:** `@ResponseStatus`를 동적으로 찾는 로직이 `AnnotationUtils` 내부에 숨겨져 있어, 처음 코드를 접하는 개발자는 동작 방식을 즉시 파악하기 어려울 수 있다. (이는 `GlobalExceptionHandler`에 명확한 주석을 추가하여 완화할 수 있다.)
