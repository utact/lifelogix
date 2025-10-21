"use client";

import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { AccessTokenResponse, api } from '@/lib/api';

interface AuthContextType {
  accessToken: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (authResponse: AccessTokenResponse) => void;
  loginWithToken: (token: string) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [accessToken, setAccessToken] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const router = useRouter();

  useEffect(() => {
    try {
      const storedToken = localStorage.getItem("accessToken");
      if (storedToken) {
        setAccessToken(storedToken);
      }
    } catch (e) { console.error("Could not access local storage", e); }
    finally { setIsLoading(false); }
  }, []);

  const login = useCallback((authResponse: AccessTokenResponse) => {
    localStorage.setItem("accessToken", authResponse.accessToken);
    setAccessToken(authResponse.accessToken);
    router.push('/dashboard');
  }, [router]);

  const loginWithToken = useCallback((token: string) => {
    localStorage.setItem("accessToken", token);
    setAccessToken(token);
    router.push('/dashboard');
  }, [router]);

  const logout = useCallback(() => {
    const token = accessToken;
    setAccessToken(null);
    localStorage.removeItem("accessToken");
    router.push('/login');
    if (token) {
      api.logout(token).catch(err => console.error("Logout API failed", err));
    }
  }, [accessToken, router]);

  const value = { accessToken, isAuthenticated: !!accessToken, isLoading, login, loginWithToken, logout };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) throw new Error('useAuth must be used within an AuthProvider');
  return context;
}