const API_BASE_URL = "/api/proxy/v1";

async function handleResponse<T>(response: Response, requestInfo: { method: string; url: string }): Promise<T> {
  const { method, url } = requestInfo;

  if (response.status === 401) {
    console.error(`[Frontend|API] <-- 401 Unauthorized ${method} ${url}. Redirecting to login.`);
    // We use a hard redirect to ensure all state is cleared.
    if (typeof window !== 'undefined') {
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      window.location.href = '/login';
    }
    // Throw an error to prevent further processing
    throw new Error("Unauthorized");
  }

  if (response.ok) {
    console.log(`[Frontend|API] <-- ${response.status} ${method} ${url} - Success`);
  } else {
    console.error(`[Frontend|API] <-- ${response.status} ${method} ${url} - Failed`);
  }

  if (response.status === 204) return Promise.resolve({} as T);

  let data;
  try {
    data = await response.json();
  } catch (error) {
    if (!response.ok) throw new Error(`Request failed with status ${response.status} and non-JSON response.`);
    return Promise.resolve({} as T);
  }

  if (!response.ok) {
    const error = data.message || `Request failed with status ${response.status}`;
    throw new Error(error);
  }
  return data as T;
}

export interface AuthResponse { accessToken: string; refreshToken: string; tokenType: string; }
export interface RegisterRequest { email: string; password: string; username: string; }
export interface LoginRequest { email: string; password: string; }
export interface Category {}
export interface ActivityGroup {}
export interface TimelineResponse {}

class ApiClient {
  private async request<T>(endpoint: string, options: RequestInit = {}, token: string | null = null): Promise<T> {
    const method = options.method || "GET";
    const url = `${API_BASE_URL}${endpoint}`;
    console.log(`[Frontend|API] --> ${method} ${url}`);

    const headers: HeadersInit = { "Content-Type": "application/json", ...options.headers };
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });
    return handleResponse<T>(response, { method, url });
  }

  async register(data: RegisterRequest): Promise<{ message: string }> {
    return this.request("/auth/register", { method: "POST", body: JSON.stringify(data) });
  }
  async login(data: LoginRequest): Promise<AuthResponse> {
    return this.request("/auth/login", { method: "POST", body: JSON.stringify(data) });
  }

  async logout(token: string): Promise<void> {
    await this.request("/auth/logout", { method: "POST" }, token);
  }
  async getTimeline(token: string, date: string): Promise<TimelineResponse> {
    return this.request(`/timeline?date=${date}`, {}, token);
  }
  async getActivities(token: string): Promise<ActivityGroup[]> {
    return this.request("/activities", {}, token);
  }
  async getCategories(token: string): Promise<Category[]> {
    return this.request("/categories", {}, token);
  }
}

export const api = new ApiClient();