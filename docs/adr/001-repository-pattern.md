# [ADR-001] 데이터 접근 계층을 위한 Repository Pattern 채택

- **Status:** Adopted
- **Last Updated:** 2025-10-19

---

## 요약 (Summary)

데이터 접근 계층의 표준 기술로 Spring Data JPA 기반의 Repository Pattern을 채택하여, 생산성과 유지보수성을 높인다.

---

## Context (컨텍스트)

프로젝트의 데이터 접근 계층(Data Access Layer) 설계 시, 비즈니스 로직과 데이터베이스 간의 의존성을 낮추고, 코드의 유지보수성과 테스트 용이성을 높이기 위한 표준화된 방식이 필요하다. 이를 위해 전통적인 DAO(Data Access Object) 패턴과 Spring Data JPA가 제공하는 Repository 패턴을 비교 검토했다.

---

## Decision (결정)

**Spring Data JPA 기반의 Repository Pattern을 데이터 접근 계층의 표준 기술로 채택한다.**

- `JpaRepository<T, ID>` 인터페이스를 상속받아 데이터 접근 인터페이스를 정의한다.
- 기본적인 CRUD(Create, Read, Update, Delete) 작업은 Spring Data JPA가 자동으로 생성하는 구현체를 활용한다.
- 복잡한 조회 조건이나 동적 쿼리는 QueryDSL을 사용하여 타입-세이프(Type-safe)하게 작성하는 것을 원칙으로 한다.

---

## Consequences (결과)

### 긍정적
- **생산성 향상:** `save()`, `findById()`, `findAll()` 등 반복적인 CRUD 코드를 직접 작성할 필요가 없어 개발 생산성이 극대화된다.
- **코드 간결성 및 가독성:** 데이터 접근 로직이 인터페이스 메소드 형태로 명확하게 드러나므로, 코드의 양이 줄고 가독성이 높아진다.
- **안정성 확보:** 컴파일 시점에 쿼리 메소드의 유효성을 검증할 수 있으며(QueryDSL 사용 시), SQL Injection과 같은 일반적인 보안 취약점으로부터 더 안전하다.
- **유지보수 용이성:** 데이터베이스 기술이 변경되더라도, 비즈니스 로직의 수정 없이 Repository 구현체만 교체하면 되므로 유연한 아키텍처를 유지할 수 있다.

### 부정적
- **학습 곡선:** Spring Data JPA와 QueryDSL의 동작 방식에 대한 이해가 필요하며, 복잡한 쿼리 작성 시 추가적인 학습이 요구될 수 있다.
- **추상화의 한계:** 매우 복잡하고 특수한 데이터베이스 연산이 필요한 경우, Repository 패턴만으로는 한계가 있을 수 있으며, 이 경우 부분적으로 JDBC나 Mybatis와 같은 기술을 혼용해야 할 수 있다.
