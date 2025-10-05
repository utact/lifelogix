# 백엔드 기술 스택 명세 및 선정 원칙

- **Version:** 1.0.0
- **Status:** Finalized
- **Last Updated:** 2025-09-28

이 문서는 LifeLogix 백엔드 프로젝트를 구성하는 핵심 기술 스택과 외부 의존성(Dependencies)을 정의하고, 각 기술을 선택한 전략적 이유를 명시합니다.

## 1. 핵심 선정 원칙 (Core Philosophy)

모든 기술 선택은 아래 4가지 핵심 원칙을 따릅니다:

1.  **Testability (테스트 용이성):** TDD 사이클을 지원하고, 테스트하기 쉬운 구조를 만들 수 있는가?
2.  **Scalability (확장성):** MVP 이후의 복잡한 기능(AI, 소셜)을 수용할 수 있는가?
3.  **Stability (안정성):** 커뮤니티가 활성화되어 있고, 장기적으로 지원되는 검증된 기술인가?
4.  **Productivity (생산성):** 개발자가 비즈니스 로직의 본질에만 집중할 수 있도록 돕는가?

## 2. 기술 스택 명세 (Dependency Specification)

| 의존성 | 핵심 역할 (Role) | 선택 이유 (Rationale)                                                          |
| :--- | :--- |:---------------------------------------------------------------------------|
| **Spring Web** | `API Contract`에 명시된 RESTful API 엔드포인트 구축 | 검증된 안정성과 방대한 생태계를 가진 산업 표준 웹 프레임워크입니다.                                     |
| **Spring Data JPA** | 도메인 객체(Entity)와 DB 테이블을 매핑하고, 데이터 영속성 로직을 관리 | DDD의 `Repository` 패턴을 가장 직관적으로 지원하며, 객체 중심의 데이터 접근을 가능하게 합니다.              |
| **PostgreSQL Driver** | `local` 프로파일에서 PostgreSQL 데이터베이스와의 통신 | 강력한 트랜잭션과 유연한 JSONB 타입을 지원하는 최적의 오픈소스 RDBMS 공식 드라이버입니다.                    |
| **H2 Database** | `test` 프로파일을 위한 인메모리(In-memory) 데이터베이스 | 빠르고 격리된 테스트 환경을 구축하여 TDD의 핵심 원칙을 지원하며, 실제 DB에 의존하지 않아 테스트의 신뢰성과 속도를 보장합니다. |
| **Lombok** | 반복적인 Boilerplate 코드 자동 생성 | 개발자가 도메인 로직의 본질에만 집중하도록 하여 TDD 효율을 극대화합니다.                                 |
| **Validation** | DTO 레벨에서 데이터 유효성 검사 규칙 적용 | 서비스 로직으로 진입하기 전, 잘못된 데이터를 차단하는 문지기(Gatekeeper) 역할을 합니다.                    |
| **Spring Boot DevTools**| 코드 변경 시 애플리케이션 자동 재시작 | 코드를 수정한 후 결과를 즉시 확인해야 하는 TDD 사이클의 속도를 극적으로 단축시킵니다.                         |
| **Spring Security** | JWT 기반 인증(Authentication) 및 인가(Authorization) 구현 | API 엔드포인트를 안전하게 보호하는 가장 강력하고 표준적인 프레임워크입니다.                                |
| **OAuth2 Resource Server** | JWT 토큰 검증 및 사용자 정보 추출 | Spring Security 환경에서 토큰 기반 인증을 매우 간단하게 구현할 수 있도록 지원합니다.                    |