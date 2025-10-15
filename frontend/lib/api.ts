const API_BASE_URL = "/api/proxy/v1";

// Helper function to handle API responses
async function handleResponse<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    // No Content
    return Promise.resolve({} as T);
  }

  const data = await response.json();

  if (!response.ok) {
    const error = data.message || `Request failed with status ${response.status}`;
    throw new Error(error);
  }
  return data as T;
}


export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  username: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

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

export interface TimeBlockActivity {
  timeBlockId: number;
  activityId: number;
  activityName: string;
  categoryName: string;
  categoryColor: string;
}

export interface TimeBlock {
  startTime: string;
  plan: TimeBlockActivity | null;
  actual: TimeBlockActivity | null;
}

export interface TimelineResponse {
  date: string;
  timeBlocks: TimeBlock[];
}

export interface CreateTimeBlockRequest {
  date: string;
  startTime: string;
  type: "PLAN" | "ACTUAL";
  activityId: number;
}

export interface UpdateTimeBlockRequest {
    activityId: number;
}

export interface CreateActivityRequest {
  name: string;
  categoryId: number;
}

export interface UpdateActivityRequest {
    name: string;
}

export interface CreateCategoryRequest {
    name: string;
    color: string;
    parentId: number;
}

export interface UpdateCategoryRequest {
    name: string;
    color: string;
}


class ApiClient {
  private getAuthHeader(): HeadersInit {
    const token = typeof window !== 'undefined' ? localStorage.getItem("accessToken") : null;
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...this.getAuthHeader(),
        ...options.headers,
      },
    });
    return handleResponse<T>(response);
  }

  // Auth
  async register(data: RegisterRequest): Promise<{ message: string }> {
    return this.request<{ message: string }>("/auth/register", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async login(data: LoginRequest): Promise<AuthResponse> {
    const response = await this.request<AuthResponse>("/auth/login", {
      method: "POST",
      body: JSON.stringify(data),
    });
    if (typeof window !== 'undefined') {
        localStorage.setItem("accessToken", response.accessToken);
        localStorage.setItem("refreshToken", response.refreshToken);
    }
    return response;
  }

  async logout(): Promise<void> {
    await this.request<void>("/auth/logout", {
        method: "POST",
    });
    if (typeof window !== 'undefined') {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
    }
  }

  async refreshToken(): Promise<{ accessToken: string }> {
      const refreshTokenValue = typeof window !== 'undefined' ? localStorage.getItem("refreshToken") : null;
      if (!refreshTokenValue) {
          throw new Error("No refresh token available.");
      }
      const response = await this.request<{ accessToken: string }>("/auth/refresh", {
          method: "POST",
          body: JSON.stringify({ refreshToken: refreshTokenValue }),
      });
      if (typeof window !== 'undefined') {
        localStorage.setItem("accessToken", response.accessToken);
      }
      return response;
  }


  // Categories
  async getCategories(): Promise<Category[]> {
    return this.request<Category[]>("/categories");
  }

  async createCategory(data: CreateCategoryRequest): Promise<Category> {
    return this.request<Category>("/categories", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateCategory(categoryId: number, data: UpdateCategoryRequest): Promise<Category> {
    return this.request<Category>(`/categories/${categoryId}`, {
        method: "PUT",
        body: JSON.stringify(data),
    });
  }

  async deleteCategory(categoryId: number): Promise<void> {
    return this.request<void>(`/categories/${categoryId}`, {
        method: "DELETE",
    });
  }

  // Activities
  async getActivities(): Promise<ActivityGroup[]> {
    return this.request<ActivityGroup[]>("/activities");
  }

  async createActivity(data: CreateActivityRequest): Promise<Activity> {
    return this.request<Activity>("/activities", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateActivity(activityId: number, data: UpdateActivityRequest): Promise<Activity> {
      return this.request<Activity>(`/activities/${activityId}`, {
          method: "PUT",
          body: JSON.stringify(data),
      });
  }

  async deleteActivity(activityId: number): Promise<void> {
      return this.request<void>(`/activities/${activityId}`, {
          method: "DELETE",
      });
  }

  // Timeline
  async getTimeline(date: string): Promise<TimelineResponse> {
    return this.request<TimelineResponse>(`/timeline?date=${date}`);
  }

  async createTimeBlock(data: CreateTimeBlockRequest): Promise<TimeBlockActivity> {
    return this.request<TimeBlockActivity>("/timeline/block", {
      method: "POST",
      body: JSON.stringify(data),
    });
  }

  async updateTimeBlock(timeBlockId: number, data: UpdateTimeBlockRequest): Promise<TimeBlockActivity> {
      return this.request<TimeBlockActivity>(`/timeline/block/${timeBlockId}`, {
          method: "PUT",
          body: JSON.stringify(data),
      });
  }

  async deleteTimeBlock(timeBlockId: number): Promise<void> {
      return this.request<void>(`/timeline/block/${timeBlockId}`, {
          method: "DELETE",
      });
  }
}

export const api = new ApiClient();