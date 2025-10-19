# [ADR-002] DTO 설계 원칙: 요청(Class)과 응답(Record) 분리

- **Status:** Superseded by [ADR-009](../009-dto-design-with-records.md)
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

요청(Request) DTO는 `class`로, 응답(Response) DTO는 `record`로 설계하여 데이터의 성격에 따라 구현 방식을 분리한다.

---

## Context (컨텍스트)

클라이언트와 서버 간의 명확하고 안정적인 데이터 교환을 위해 DTO(Data Transfer Object) 설계에 대한 일관된 규칙 수립이 필요하다. 특히 데이터의 성격(요청/응답)에 따라 Java의 `class`와 `record`를 전략적으로 사용하는 것을 목표로 한다.

---

## Decision (결정)

**요청(Request) 데이터는 `Class`로, 응답(Response) 데이터는 `Record`로 설계한다.**

- **요청 DTO (Request DTOs)**
    - **정의**: 클라이언트가 서버로 데이터를 보낼 때 사용하는 객체 (e.g., `RegisterRequest.java`)
    - **구현**: 전통적인 `Class` (POJO)와 `@Setter`를 사용한다.
    - **선택 이유**:
        1.  **프레임워크 호환성**: Spring은 JSON 요청을 자바 객체로 변환(역직렬화)할 때, 내부적으로 **기본 생성자**와 **Setter**를 사용하는 방식에 가장 안정적으로 최적화되어 있다. 이는 외부 라이브러리와의 유연하고 강력한 데이터 바인딩을 보장한다.
        2.  **유효성 검사(@Valid) 용이성**: `@Valid`를 통한 유효성 검사 프레임워크 또한 Setter를 통해 값이 할당된 객체를 검증하는 방식과 자연스럽게 통합된다.

- **응답 DTO (Response DTOs)**
    - **정의**: 서버가 클라이언트로 데이터를 보낼 때 사용하는 객체 (e.g., `TokenResponse.java`)
    - **구현**: Java `Record`를 사용한다.
    - **선택 이유**:
        1.  **데이터 불변성(Immutability)**: `record`로 생성된 객체는 필드 값을 변경할 수 없다. 서버가 생성한 응답 데이터가 다른 로직에 의해 의도치 않게 변경되는 **부작용(Side Effect)을 원천적으로 차단**하여 코드의 예측 가능성과 안정성을 크게 향상시킨다.
        2.  **코드 간결성(Conciseness)**: `record`는 생성자, Getter, `equals()`, `hashCode()`, `toString()`을 자동으로 생성해 주므로, 개발자는 데이터 필드 선언에만 집중할 수 있어 보일러플레이트 코드가 획기적으로 줄어든다.

---

## Consequences (결과)

데이터의 흐름과 목적에 따라 DTO의 구현 방식을 명확히 구분한다. **외부로부터의 유연한 데이터 수용이 필요한 요청(Request)은** `class`를 사용하고, **내부에서 생성되어 일관성이 보장되어야 하는 응답(Response)은** 불변성을 제공하는 `record`를 사용하여 프로젝트 전체의 안정성과 생산성을 높인다.
