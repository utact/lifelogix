import { type NextRequest, NextResponse } from "next/server"

const BACKEND_URL = "https://lifelogix-dca5.onrender.com/api/v1";

export async function GET(request: NextRequest, { params }: { params: { path: string[] } }) {
  const path = params.path.join("/")
  const searchParams = request.nextUrl.searchParams.toString()
  const url = `${BACKEND_URL}/${path}${searchParams ? `?${searchParams}` : ""}`

  const headers: HeadersInit = {}
  const authHeader = request.headers.get("authorization")
  if (authHeader) {
    headers.Authorization = authHeader
  }

  try {
    const response = await fetch(url, { headers })
    const data = await response.json()

    return NextResponse.json(data, { status: response.status })
  } catch (error) {
    return NextResponse.json({ message: "Backend request failed" }, { status: 500 })
  }
}

export async function POST(request: NextRequest, { params }: { params: { path: string[] } }) {
  const path = params.path.join("/")
  const url = `${BACKEND_URL}/${path}`
  const body = await request.json()

  console.log("[v0 Proxy] POST request to:", url)
  console.log("[v0 Proxy] Request body:", body)

  const headers: HeadersInit = {
    "Content-Type": "application/json",
  }
  const authHeader = request.headers.get("authorization")
  if (authHeader) {
    headers.Authorization = authHeader
  }

  try {
    const response = await fetch(url, {
      method: "POST",
      headers,
      body: JSON.stringify(body),
    })

    console.log("[v0 Proxy] Backend response status:", response.status)

    const data = await response.json()
    console.log("[v0 Proxy] Backend response data:", data)

    return NextResponse.json(data, { status: response.status })
  } catch (error) {
    console.error("[v0 Proxy] Backend request error:", error)
    return NextResponse.json({ message: "Backend request failed", error: String(error) }, { status: 500 })
  }
}

export async function PUT(request: NextRequest, { params }: { params: { path: string[] } }) {
  const path = params.path.join("/")
  const url = `${BACKEND_URL}/${path}`
  const body = await request.json()

  const headers: HeadersInit = {
    "Content-Type": "application/json",
  }
  const authHeader = request.headers.get("authorization")
  if (authHeader) {
    headers.Authorization = authHeader
  }

  try {
    const response = await fetch(url, {
      method: "PUT",
      headers,
      body: JSON.stringify(body),
    })
    const data = await response.json()

    return NextResponse.json(data, { status: response.status })
  } catch (error) {
    return NextResponse.json({ message: "Backend request failed" }, { status: 500 })
  }
}

export async function DELETE(request: NextRequest, { params }: { params: { path: string[] } }) {
  const path = params.path.join("/")
  const url = `${BACKEND_URL}/${path}`

  const headers: HeadersInit = {}
  const authHeader = request.headers.get("authorization")
  if (authHeader) {
    headers.Authorization = authHeader
  }

  try {
    const response = await fetch(url, {
      method: "DELETE",
      headers,
    })
    const data = await response.json()

    return NextResponse.json(data, { status: response.status })
  } catch (error) {
    return NextResponse.json({ message: "Backend request failed" }, { status: 500 })
  }
}
