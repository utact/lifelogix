"use client";

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';
import { useToast } from "@/hooks/use-toast";
import { api } from '@/lib/api';

export default function OAuthRedirectPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { loginWithToken } = useAuth();
  const { toast } = useToast();

  useEffect(() => {
    const code = searchParams.get('code');

    if (code) {
      api.exchangeCodeForToken(code)
        .then(response => {
          const { accessToken } = response;
          loginWithToken(accessToken);
          toast({ title: "로그인 성공", description: "소셜 로그인이 완료되었습니다." });
          router.replace('/dashboard');
        })
        .catch(error => {
          console.error("OAuth token exchange failed:", error);
          toast({
            title: "인증 실패",
            description: "로그인에 실패했습니다. 다시 시도해주세요.",
            variant: "destructive",
          });
          router.replace('/login');
        });
    } else {
      // This case might happen if the user navigates to this page directly
      // or if the provider returns an error.
      toast({
        title: "인증 오류",
        description: "유효하지 않은 접근입니다. 로그인 페이지로 이동합니다.",
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
