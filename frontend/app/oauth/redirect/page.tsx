"use client";

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useToast } from "@/hooks/use-toast";

export default function OAuthRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { loginWithToken } = useAuth();
  const { toast } = useToast();

  useEffect(() => {
    const token = searchParams.get('token');

    if (token) {
      loginWithToken(token);
      toast({ title: "로그인 성공", description: "소셜 로그인이 완료되었습니다." });
      router.replace('/dashboard');
    } else {
      toast({
        title: "인증 실패",
        description: "소셜 로그인에 실패했습니다. 다시 시도해주세요.",
        variant: "destructive",
      });
      router.replace('/login');
    }
  }, [router, searchParams, loginWithToken, toast]);

  return (
    <div className="flex min-h-screen items-center justify-center">
      <div className="text-muted-foreground">인증 정보를 처리 중입니다...</div>
    </div>
  );
}
