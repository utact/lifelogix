"use client";

import { useEffect, type ReactNode } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/AuthContext';

import HelpIcon from '@/components/HelpIcon';

const FullScreenLoader = () => (
  <div className="flex h-screen items-center justify-center bg-background">
    <div className="text-muted-foreground">Verifying authentication...</div>
  </div>
);

export default function ProtectedLayout({ children }: { children: ReactNode }) {
  const { isAuthenticated, isLoading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.replace('/login');
    }
  }, [isLoading, isAuthenticated, router]);

  if (isLoading || !isAuthenticated) {
    return <FullScreenLoader />;
  }

  return (
    <>
      {children}
      <HelpIcon />
    </>
  );
}