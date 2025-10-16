# Next.js 프록시 설정 오류 해결 및 구조 개선 문서

> **문서 요약:** 본 문서는 `next.config.mjs`의 `rewrites`와 API 라우트 핸들러(`app/api/proxy/...`)의 프록시 기능이 충돌하여 발생한 `401 Unauthorized` 에러의 원인을 분석하고, API 라우트 핸들러로 프록시 역할을 일원화하여 문제를 해결하는 과정을 기록합니다.

---

## 1. 문제 상황 (Problem)

Render에 배포된 Spring Boot 백엔드 서버는 정상적으로 구동되고 있었으나, 프론트엔드에서 로그인을 시도할 때 지속적으로 `401 Unauthorized` 에러가 발생했습니다.

- **증상 1:** 브라우저 콘솔에 `POST /api/proxy/v1/auth/login 401 (Unauthorized)` 에러 출력
- **증상 2:** 백엔드 서버는 정상 실행 중임에도 불구하고, Render 대시보드 로그에 로그인 요청 관련 로그가 전혀 기록되지 않음

이 두 가지 증상은 요청이 백엔드의 서비스 로직(`UserService`)에 도달하기 전, Spring Security 필터 단계에서 차단되고 있음을 시사했습니다.

## 2. 원인 분석 (Root Cause Analysis)

문제의 핵심 원인은 **API 요청 경로가 중복되어 잘못된 URL로 백엔드에 전송**되고 있었기 때문입니다.

- **기대 경로:** `https://[백엔드-주소]/api/v1/auth/login`
- **실제 요청 경로:** `https://[백엔드-주소]/api/v1/v1/auth/login`

이러한 경로 중복은 Next.js 프로젝트 내에 두 가지 프록시 설정이 혼재하고, 그중 하나가 잘못 구성되었기 때문에 발생했습니다.

1.  **`next.config.mjs`의 `rewrites` 함수:** Next.js의 내장 프록시 기능
2.  **API 라우트 핸들러 (`/app/api/proxy/[...path]/route.ts`):** 직접 구현한 프록시 서버

Next.js에서는 **API 라우트 핸들러가 `rewrites`보다 우선순위가 높기 때문에**, 모든 `/api/proxy/**` 요청은 우리가 직접 만든 `handler` 함수에 의해 처리되고 있었습니다.

문제는 `handler` 함수 내부의 URL 조합 로직에 있었습니다.

**`app/api/proxy/[...path]/route.ts` (수정 전):**
```typescript
// BACKEND_URL에 이미 '/api/v1'이 포함되어 있음
const BACKEND_URL = 'https://lifelogix-dca5.onrender.com/api/v1';

async function handler(req: NextRequest) {
  // 프론트에서 넘어온 path: '/v1/auth/login'
  const path = req.nextUrl.pathname.replace('/api/proxy', '');

  // 최종 url: '.../api/v1' + '/v1/auth/login' => 경로 중복 발생!
  const url = `${BACKEND_URL}${path}`;
  // ...
}
```

결과적으로 Spring 백엔드는 `/api/v1/v1/auth/login`이라는 존재하지 않는 엔드포인트로 요청을 받아 `permitAll()` 규칙에 해당하지 않으므로 `401` 에러를 반환했던 것입니다.

## 3. 해결 과정 (Solution)

문제를 해결하기 위해 프록시 역할을 **API 라우트 핸들러로 일원화**하고, 중복된 설정을 제거하는 방향으로 코드를 수정했습니다.

### 단계 1: API 라우트 핸들러의 URL 생성 로직 수정

`BACKEND_URL`에서 중복되는 경로를 제거하고, 핸들러 내에서 올바른 전체 경로를 조합하도록 수정했습니다.

**`app/api/proxy/[...path]/route.ts` (수정 후):**
```typescript
// '/api/v1' 부분을 제거하여 순수 도메인만 남김
const BACKEND_URL = 'https://lifelogix-dca5.onrender.com';

async function handler(req: NextRequest) {
  const path = req.nextUrl.pathname.replace('/api/proxy', ''); // path = '/v1/auth/login'
  
  // 백엔드의 기본 API 경로인 '/api'를 직접 붙여 올바른 URL을 생성
  const url = `${BACKEND_URL}/api${path}`; // 최종 url = '.../api/v1/auth/login'
  // ...
}
```

### 단계 2: `next.config.mjs`에서 중복 기능 제거

API 라우트 핸들러가 모든 프록시 역할을 담당하게 되었으므로, 더 이상 필요 없어진 `rewrites` 함수를 `next.config.mjs`에서 완전히 삭제하여 설정 파일의 역할을 명확히 했습니다.

**`next.config.mjs` (수정 후):**
```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  // ... 기존 eslint, typescript 설정 등
  
  // rewrites 함수를 완전히 제거
};

export default nextConfig;
```

## 4. 결과 (Result)

-   프록시 요청 경로가 `https://.../api/v1/auth/login`으로 정상적으로 수정되었습니다.
-   로그인 시 `401` 에러가 사라지고 정상적으로 인증 토큰을 수신합니다.
-   Render 대시보드에 백엔드 `UserService`의 로그가 정상적으로 출력되는 것을 확인했습니다.
-   프록시 로직을 API 라우트 핸들러로 일원화하여 프로젝트 구조가 더 명확해졌습니다.