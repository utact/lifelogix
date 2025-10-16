import { NextRequest, NextResponse } from 'next/server';

const BACKEND_URL = 'https://lifelogix-dca5.onrender.com/api/v1';

async function handler(req: NextRequest) {
  const path = req.nextUrl.pathname.replace('/api/proxy', '');
  const searchParams = req.nextUrl.searchParams.toString();
  const url = `${BACKEND_URL}${path}${searchParams ? `?${searchParams}` : ''}`;

  // Forward all headers from the original request except for the host.
  const headers = new Headers(req.headers);
  headers.delete('host');

  // Read the body as text to ensure the payload is forwarded without modification.
  const body = await req.text();

  try {
    const response = await fetch(url, {
      method: req.method,
      headers,
      // Pass the body as a string, or null if it's empty.
      body: body || null,
      redirect: 'manual',
    });

    // Create a new response from the backend's response to ensure clean headers.
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
      { status: 502 } // Bad Gateway
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
