# Contributing to LifeLogix

LifeLogix 프로젝트에 기여해주셔서 감사합니다. 원활한 협업과 코드 품질 유지를 위해 모든 기여자는 아래의 가이드라인을 따라주시길 바랍니다.

## 1. 개발 워크플로우

모든 개발은 `develop` 브랜치에서 시작하여, 기능별 브랜치(feature branch)에서 작업을 진행한 후 Pull Request(PR)를 통해 다시 `develop` 브랜치로 병합하는 것을 원칙으로 합니다.

1.  **이슈(Issue) 확인 및 생성**: 작업 시작 전, 관련된 이슈가 있는지 확인하고 없다면 새로 생성합니다.
2.  **브랜치 생성**: `develop` 브랜치에서 새로운 기능 브랜치를 생성합니다.
3.  **TDD 기반 개발**: 테스트 코드를 먼저 작성하거나, 기능 구현과 테스트 코드 작성을 병행합니다.
4.  **테스트 실행**: `./gradlew clean test` 명령어로 모든 테스트가 통과하는지 확인합니다.
5.  **Pull Request(PR) 생성**: `develop` 브랜치로 향하는 PR을 생성합니다.
6.  **코드 리뷰 및 병합**: 동료의 코드 리뷰를 거친 후 PR을 병합(Merge)합니다.

## 2. 브랜치 전략

-   **`main`**: 실제 배포 버전이 위치하는 브랜치.
-   **`develop`**: 다음 배포 버전을 개발하는 메인 브랜치.
-   **기능 브랜치**: `develop`에서 분기하며, 접두사를 사용하여 브랜치의 목적을 명확히 합니다.
    -   `feat/feature-name` (e.g., `feat/activity`)
    -   `fix/bug-name` (e.g., `fix/login-error`)
    -   `refactor/scope` (e.g., `refactor/dto`)
    -   `docs/document-name` (e.g., `docs/add-adr`)

## 3. 커밋 메시지 컨벤션

모든 커밋 메시지는 **영어로 작성**하며, **Conventional Commits** 명세를 따릅니다.

**형식:** `<type>(<scope>): <subject>`

-   **`<type>` 종류**:
    -   `feat`: 새로운 기능 추가
    -   `fix`: 버그 수정
    -   `refactor`: 기능 변경 없는 코드 구조 개선
    -   `docs`: 문서 수정 (ADR, API Contract, README 등)
    -   `test`: 테스트 코드 추가 또는 수정
    -   `chore`: 빌드, 의존성 등 기타 유지보수 작업
    -   `style`: 코드 포맷팅, 세미콜론 등 스타일 관련 수정

## 4. Pull Request(PR) 컨벤션

-   **제목**: 영어로, `<Type>: <Description>` 형식으로 작성합니다. (e.g., `feat: Implement Timeline Domain Layer`)
-   **본문**: 한글로, 아래 템플릿에 맞춰 작성합니다.
    ```markdown
    ## 🚀 작업 배경
    (이 작업이 왜 필요한지, 어떤 배경에서 시작되었는지 설명)

    ## 🛠️ 주요 변경 사항
    (어떤 내용이 변경되었는지 명확하고 간결하게 요약)

    ## 🔗 관련 이슈
    - Resolves #이슈번호
    ```

## 5. 아키텍처 및 코딩 컨벤션

### 5.1. 패키지 구조

-   프로젝트는 **'도메인 중심 모듈형 아키텍처'를** 따릅니다.
-   상세한 계층 구조와 규칙은 `ADR-008`, `ADR-009`를 참조하십시오.

### 5.2. DTO 설계

-   모든 DTO(Request, Response)는 Java **`record` 타입을 우선적으로 사용**합니다.
-   상세 내용은 `ADR-010`을 참조하십시오.

### 5.3. 테스트 전략

-   **Assertion 스타일**:
    -   모든 테스트 검증은 AssertJ 라이브러리를 사용하며, `org.assertj.core.api.Assertions` 클래스를 `static import`하여 사용합니다.
        ```java
        import static org.assertj.core.api.Assertions.assertThat;
        import static org.assertj.core.api.Assertions.assertThatThrownBy;
        ```
-   **테스트명**:
    -   **메서드명**: 한글 BDD 스타일로, 테스트 시나리오를 서술합니다. (e.g., `사용자_정의_카테고리를_성공적으로_생성한다()`)
    -   **`@DisplayName`**: 테스트 그룹을 대표하는 간결한 '제목'을 한글로 작성합니다. (e.g., `"사용자 정의 카테고리 생성"`)
-   **테스트 데이터 생성**:
    -   서비스 단위 테스트에서 Mock 엔티티 데이터가 필요한 경우, 엔티티 내부에 `@deprecated public` 생성자를 추가하여 사용합니다.
    -   상세 내용은 `ADR-011`을 참조하십시오.
-   **테스트 계층**:
    -   **`@DataJpaTest`**: Repository 및 Entity 검증.
    -   **`@ExtendWith(MockitoExtension.class)`**: Service 계층 단위 테스트.
    -   **`@SpringBootTest`**: Controller를 포함한 API End-to-End 통합 테스트.

## 6. 문서화

-   중요한 아키텍처 변경이나 기술 선택 시에는 `docs/adr` 폴더에 ADR 문서를 작성합니다.
-   API 변경 시에는 `docs/api` 폴더의 API Contract 문서를 시맨틱 버저닝 규칙에 따라 업데이트하거나 새로 생성합니다.