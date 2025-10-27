const API_BASE_URL = "/api/proxy/v1";

async function handleResponse<T>(response: Response, requestInfo: { method: string; url: string }): Promise<T> {
  const { method, url } = requestInfo;

  if (response.status === 401) {
    console.error(`[Frontend|API] <-- 401 Unauthorized ${method} ${url}. Redirecting to login.`);
    if (typeof window !== 'undefined') {
      localStorage.removeItem("accessToken");
      window.location.href = '/login';
    }
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

export interface AccessTokenResponse { accessToken: string; tokenType: string; }
export interface OAuthTokenResponse { accessToken: string; }
export interface RegisterRequest { email: string; password: string; username: string; }
export interface LoginRequest { email: string; password: string; }
export interface Category {
  id: number;
  name: string;
  color: string;
  isCustom: boolean;
  parentId: number | null;
}
export interface Activity {
  id: number;
  name: string;
}
export interface ActivityGroup {
  categoryId: number;
  categoryName: string;
  activities: Activity[];
}
export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  createdAt: string;
}

export interface CreateTimeBlockRequest {
  date: string;
  startTime: string;
  type: 'PLAN' | 'ACTUAL';
  activityId: number;
}

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

  async exchangeCodeForToken(code: string): Promise<OAuthTokenResponse> {
    return this.request("/auth/oauth-token", {
      method: "POST",
      body: JSON.stringify({ code }),
    });
  }

  async register(data: RegisterRequest): Promise<{ message: string }> {
    return this.request("/auth/register", { method: "POST", body: JSON.stringify(data) });
  }
  async login(data: LoginRequest): Promise<AccessTokenResponse> {
    return this.request("/auth/login", { method: "POST", body: JSON.stringify(data) });
  }
  async logout(token: string): Promise<void> {
    await this.request("/auth/logout", { method: "POST" }, token);
  }
  async getMe(token: string): Promise<UserResponse> {
    return this.request('/users/me', {}, token);
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
  async createCategory(token: string, data: { name: string; color: string; parentId: number; }): Promise<Category> {
    return this.request("/categories", { method: "POST", body: JSON.stringify(data) }, token);
  }
  async createActivity(token: string, data: { name: string; categoryId: number; }): Promise<Activity> {
    return this.request("/activities", { method: "POST", body: JSON.stringify(data) }, token);
  }
  async updateCategory(token: string, id: number, data: { name: string; color: string; }): Promise<Category> {
    return this.request(`/categories/${id}`, { method: "PUT", body: JSON.stringify(data) }, token);
  }
  async deleteCategory(token: string, id: number): Promise<void> {
    return this.request(`/categories/${id}`, { method: "DELETE" }, token);
  }
  async updateActivity(token: string, id: number, data: { name: string; }): Promise<Activity> {
    return this.request(`/activities/${id}`, { method: "PUT", body: JSON.stringify(data) }, token);
  }
  async deleteActivity(token: string, id: number): Promise<void> {
    return this.request(`/activities/${id}`, { method: "DELETE" }, token);
  }

  async createTimeBlock(token: string, data: CreateTimeBlockRequest): Promise<void> {
    return this.request("/timeline/block", { method: "POST", body: JSON.stringify(data) }, token);
  }
}

export const api = new ApiClient();