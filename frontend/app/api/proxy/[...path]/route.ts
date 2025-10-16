import { NextRequest, NextResponse } from 'next/server';

// 환경 변수에서 백엔드 URL을 가져오거나 기본값을 사용
const BACKEND_URL = process.env.BACKEND_URL || 'https://lifelogix-dca5.onrender.com';

async function handler(req: NextRequest) {
  // 요청 경로에서 '/api/proxy' 부분을 제거하여 실제 API 경로를 추출
  // 예: '/api/proxy/v1/auth/login' -> '/v1/auth/login'
  const path = req.nextUrl.pathname.replace('/api/proxy', '');

  // 백엔드 API의 전체 URL을 구성
  const url = `${BACKEND_URL}/api${path}${req.nextUrl.search}`;

  console.log('[API Proxy] Incoming request:', req.method, req.nextUrl.pathname);
  console.log('[API Proxy] Forwarding to:', url);

  // 클라이언트로부터 받은 헤더를 복사하고, Authorization 헤더 존재 여부를 로깅
  const requestHeaders = new Headers(req.headers);
  requestHeaders.set('Host', new URL(url).host);
  console.log('[API Proxy] Authorization Header Present:', requestHeaders.has('Authorization'));

  try {
    // 백엔드로 요청을 전달
    const response = await fetch(url, {
      method: req.method,
      headers: requestHeaders,
      // GET/HEAD 요청이 아닐 경우 본문을 포함
      body: req.method !== 'GET' && req.method !== 'HEAD' ? req.body : null,
      // Next.js 13+에서는 duplex가 필요
      // @ts-ignore
      duplex: 'half',
    });

    // 백엔드로부터 받은 응답을 클라이언트로 전달하기 위해 새로 구성
    // Content-Encoding 헤더 문제를 피하기 위해 본문을 직접 읽고 헤더를 선별적으로 복사
    const body = await response.arrayBuffer();
    const responseHeaders = new Headers();
    responseHeaders.set('Content-Type', response.headers.get('Content-Type') || 'application/json');

    return new NextResponse(body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });

  } catch (error) {
    console.error(`[API Proxy] Error forwarding request to ${url}:`, error);
    return NextResponse.json({ message: 'Proxy request failed' }, { status: 500 });
  }
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const DELETE = handler;
export const PATCH = handler;
export const HEAD = handler;
export const OPTIONS = handler;