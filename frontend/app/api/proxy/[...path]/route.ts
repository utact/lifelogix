import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = 'https://lifelogix-dca5.onrender.com';

async function handler(req: NextRequest) {
  // req.nextUrl.pathname이 '/api/proxy/v1/auth/login' 이라면 path는 '/v1/auth/login'
  const path = req.nextUrl.pathname.replace('/api/proxy', '');
  const searchParams = req.nextUrl.searchParams.toString();

  // 최종 url은 'https://.../api/v1/auth/login'
  const url = `${BACKEND_URL}/api${path}${searchParams ? `?${searchParams}` : ''}`;

  const headers = new Headers(req.headers);
  headers.delete('host');

  const body = await req.text();

  try {
    const response = await fetch(url, {
      method: req.method,
      headers,
      body: body || null,
      redirect: 'manual',
    });

    const responseHeaders = new Headers(response.headers);

    return new NextResponse(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });

  } catch (error) {
    console.error(`[API Proxy] Error fetching ${url}:`, error);
    return NextResponse.json(
      { message: 'Proxy error', error: String(error) },
      { status: 502 }
    );
  }
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const DELETE = handler;
export const PATCH = handler;
export const HEAD = handler;
export const OPTIONS = handler;
