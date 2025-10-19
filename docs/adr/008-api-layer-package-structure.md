# [ADR-008] API 계층 내부 패키지 구조 세분화

- **Status:** Adopted
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

API 계층(`api` 패키지)의 가독성과 유지보수성을 높이기 위해, 내부 구조를 `controller`와 `dto`로, `dto`는 다시 `request`와 `response`로 세분화한다.

---

## Context (컨텍스트)

도메인 중심 패키지 구조(`ADR-007`)에서 `api` 패키지는 외부와의 통신을 담당하는 컨트롤러와 DTO(Data Transfer Object)를 포함한다. 기능이 확장될수록 `api` 패키지 하나에 모든 관련 클래스가 혼재하면 가독성과 유지보수성이 저하될 수 있으므로, 내부 구조를 체계적으로 세분화할 필요가 있다.

---

## Decision (결정)

**`api` 패키지 내부를 `controller`와 `dto`로, `dto`는 다시 `request`와 `response`로 세분화한다.**

-   **`api/controller`**: `@RestController` 클래스를 위치시킨다. 이 컨트롤러는 HTTP 요청을 받아 `application` 계층의 서비스와 상호작용하는 역할만 수행한다.
-   **`api/dto`**: 데이터 전송 객체를 위치시킨다.
    -   **`dto/request`**: 외부 시스템에서 API로 들어오는 요청 본문(Request Body)을 매핑하는 DTO를 위치시킨다.
    -   **`dto/response`**: API가 외부 시스템으로 반환하는 응답 본문(Response Body)을 구성하는 DTO를 위치시킨다.

```
api
├── controller
│   └── UserController.java
└── dto
    ├── request
    │   └── UserRegisterRequest.java
    └── response
        └── UserResponse.java
```

---

## Consequences (결과)

### 긍정적
- **명확한 역할 분리:** 컨트롤러와 데이터 객체의 물리적 분리를 통해 각 컴포넌트의 역할이 명확해지고, 파일 탐색이 용이해진다.
- **데이터 흐름의 가시성:** `request`와 `response`의 분리는 API를 통한 데이터의 유입과 유출을 명시적으로 보여주어, 코드의 의도를 파악하기 쉽게 만든다.
- **유지보수성 향상:** 기능이 복잡해져 DTO의 종류가 많아지더라도, 체계적인 구조 덕분에 파일을 관리하고 재사용하기 용이하다.

### 부정적
- **구조적 깊이 증가:** 패키지 구조의 깊이가 다소 깊어질 수 있으나, 이는 명확성 확보를 위한 허용 가능한 수준의 복잡도이다.
