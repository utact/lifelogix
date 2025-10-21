import { NextRequest, NextResponse } from 'next/server';

// 환경 변수에서 백엔드 URL을 가져오거나 환경(NODE_ENV)에 따라 기본값 설정
// '/api/v1' 경로 제거
const BACKEND_URL_BASE = process.env.BACKEND_URL ||
                    (process.env.NODE_ENV === 'development'
                      ? 'http://localhost:8080' // 개발 환경일 경우 로컬 백엔드
                      : 'https://lifelogix-dca5.onrender.com'); // 운영 환경일 경우 배포된 백엔드

async function handler(req: NextRequest) {
  // 요청 경로에서 '/api/proxy' 부분을 제거하여 실제 API 경로를 추출
  const path = req.nextUrl.pathname.replace('/api/proxy', '');

  // 백엔드 API의 전체 URL을 구성
  let url;
  if (path.startsWith('/oauth2/authorization')) {
    // OAuth2 인증 경로는 '/api' 접두사 없이 백엔드 루트 경로로 구성
    url = `${BACKEND_URL_BASE}${path}${req.nextUrl.search}`;
    console.log(`[API Proxy][${process.env.NODE_ENV || 'unknown'}] OAuth path detected. Forwarding without /api prefix.`);
  } else {
    // 그 외 API는 '/api' 접두사 추가 (기존 v1 API 등)
    url = `${BACKEND_URL_BASE}/api${path}${req.nextUrl.search}`;
  }

  // 현재 환경(development/production)과 함께 로그 출력
  console.log(`[API Proxy][${process.env.NODE_ENV || 'unknown'}] Incoming request:`, req.method, req.nextUrl.pathname);
  console.log(`[API Proxy][${process.env.NODE_ENV || 'unknown'}] Forwarding to:`, url);

  // 클라이언트로부터 받은 헤더를 복사
  const requestHeaders = new Headers(req.headers);
  // Host 헤더를 백엔드 서버의 호스트로 변경
  requestHeaders.set('Host', new URL(url).host);
  // Authorization 헤더 존재 여부 로깅 (디버깅용)
  console.log('[API Proxy] Authorization Header Present:', requestHeaders.has('Authorization'));
  // Origin 헤더 제거 (CORS 문제 발생 시 필요)
  requestHeaders.delete('origin');
  requestHeaders.delete('referer');


  try {
    // 백엔드로 요청을 전달
    const response = await fetch(url, {
      method: req.method,
      headers: requestHeaders,
      // GET/HEAD 요청이 아닐 경우 본문을 포함하여 전달
      body: req.method !== 'GET' && req.method !== 'HEAD' ? req.body : null,
      // Next.js App Router 환경에서 fetch 요청 시 필요
      // @ts-ignore - duplex 타입 에러 무시
      duplex: 'half',
      // 백엔드가 자체 서명된 인증서 등을 사용하는 경우에만 필요 (일반적으로 불필요)
      // cache: 'no-store', // 필요시 캐시 비활성화
    });

    // 백엔드 응답을 클라이언트로 전달하기 위한 헤더 생성
    const responseHeaders = new Headers();
    // 필수 헤더 복사 (예: Content-Type)
    responseHeaders.set('Content-Type', response.headers.get('Content-Type') || 'application/json');
    // CORS 관련 헤더는 백엔드 서버에서 설정하는 것이 일반적
    // 필요 시 여기에 추가: responseHeaders.set('Access-Control-Allow-Origin', '*');

    // ArrayBuffer로 본문을 읽어 새 응답 생성 (Content-Encoding 문제 회피)
    const body = await response.arrayBuffer();

    // 클라이언트로 최종 응답 반환
    return new NextResponse(body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });

  } catch (error) {
    console.error(`[API Proxy][${process.env.NODE_ENV || 'unknown'}] Error forwarding request to ${url}:`, error);
    // 오류 발생 시 500 에러와 메시지 반환
    return NextResponse.json({ message: 'Proxy request failed', error: error instanceof Error ? error.message : String(error) }, { status: 500 });
  }
}

// 모든 HTTP 메소드에 대해 동일한 핸들러 함수를 사용하도록 export
export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const DELETE = handler;
export const PATCH = handler;
export const HEAD = handler;
export const OPTIONS = handler;