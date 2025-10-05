# [ADR-004] API 엔드포인트 버전 관리 전략

- **Version:** 1.0.0
- **Status:** Finalized
- **Last Updated:** 2025-10-05

이 문서는 LifeLogix API의 변경 사항을 관리하고, 향후 모바일 클라이언트나 외부 서비스 연동 시 발생할 수 있는 호환성 문제를 예방하기 위한 엔드포인트 버전 관리(Versioning) 전략을 정의합니다.

## 1. 배경 (Background)

- API는 서비스가 발전함에 따라 필연적으로 변경됩니다. 특히 기존 기능의 요청/응답 구조가 바뀌는 '파괴적 변경(Breaking Changes)'이 발생했을 때, 구버전 클라이언트의 동작을 보장하기 위한 안정적인 전략이 필요합니다.

## 2. 고려된 대안 (Alternatives Considered)

API 버전 관리를 위한 주요 방식들은 다음과 같습니다.

| 방식 | 예시 | 장점 | 단점 |
| :--- | :--- | :--- | :--- |
| **1. URL 경로 기반** | `/api/v1/timeline` | 가장 명시적이고 직관적인 방식 | URL이 다소 길어짐 |
| **2. 쿼리 파라미터 기반**| `/api/timeline?version=1`| URL 경로의 간결함 유지 | 버전 미명시 시 기본값 정책 필요 |
| **3. 커스텀 헤더 기반**| `Accept: application/vnd.lifelogix.v1+json`| URL을 오염시키지 않는 가장 순수한 방식 | 브라우저 주소창만으로는 테스트 불가 |

## 3. 의사결정 (Decision)

**LifeLogix 프로젝트는 'URL 경로 기반(URL Path Versioning)'의 API 버전 관리 전략을 채택합니다.**

이에 따라 모든 API 엔드포인트는 `/api/{version}`의 기본 경로(Base Path)를 가집니다. (e.g., `/api/v1`)

## 4. 선택 이유 (Rationale)

1.  **명확성과 단순성 (Clarity & Simplicity):**
    -   URL에 버전이 명시되어 있어, 개발자가 엔드포인트 주소만 보고도 어떤 버전의 API를 사용하는지 즉시 파악할 수 있습니다. 이는 API를 소비하는 클라이언트 개발자의 혼란을 최소화하는 가장 확실한 방법입니다.

2.  **사용성과 접근성 (Usability & Accessibility):**
    -   별도의 헤더 설정 없이 브라우저 주소창에 URL을 입력하는 것만으로도 API 테스트가 가능하여, 개발 및 디버깅 과정의 생산성을 크게 향상시킵니다. Postman, curl 등 모든 HTTP 클라이언트에서 가장 간단하게 사용할 수 있습니다.

URL 경로에 버전을 명시하는 것이 주는 명확성의 이점이 다른 대안들의 장점을 압도한다고 판단했습니다. 이 전략은 우리 프로젝트의 핵심 원칙인 **생산성(Productivity)과 안정성(Stability)에** 가장 부합합니다.