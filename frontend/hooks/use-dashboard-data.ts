"use client"

import { useState, useEffect, useCallback } from 'react';
import { api, type TimelineResponse, type ActivityGroup } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';

// API 응답 타입이 any로 되어 있어, 실제 타입으로 명시
interface TimelineData extends TimelineResponse {
  timeBlocks: any[]; 
}
interface ActivitiesData extends ActivityGroup {
  // 필요 시 ActivityGroup의 상세 타입을 여기에 정의
}


export function useDashboardData(selectedDate: Date) {
  // 항상 빈 배열로 초기화하여 .length 에러를 원천적으로 방지
  const [timeBlocks, setTimeBlocks] = useState<any[]>([]);
  const [activities, setActivities] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const { accessToken, isAuthenticated } = useAuth();

  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  const fetchData = useCallback(async () => {
    // 인증된 상태이고, 토큰이 존재할 때만 API를 호출
    if (!isAuthenticated || !accessToken) {
      // 로딩이 필요 없는 경우 즉시 종료
      if(!isLoading) setIsLoading(true); // 로딩 시작 표시
      return;
    }

    const dateStr = formatDate(selectedDate);
    
    try {
      setIsLoading(true);
      setError(null);
      const [timelineData, activitiesData] = await Promise.all([
        api.getTimeline(accessToken, dateStr),
        api.getActivities(accessToken),
      ]);
      
      // API 응답이 없을 경우를 대비하여 항상 배열을 보장
      setTimeBlocks((timelineData as TimelineData)?.timeBlocks || []);
      setActivities(activitiesData || []);

    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch dashboard data'));
      console.error(`[Dashboard] Fetching failed for date: ${dateStr}`, err);
      // 에러 발생 시에도 빈 배열로 초기화하여 UI 깨짐을 방지
      setTimeBlocks([]);
      setActivities([]);
    } finally {
      setIsLoading(false);
    }
  }, [selectedDate, accessToken, isAuthenticated]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { timeBlocks, activities, isLoading, error, refetch: fetchData };
}