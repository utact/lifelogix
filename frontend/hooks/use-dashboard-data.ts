"use client"

import { useState, useEffect, useCallback } from 'react';
import { api, type TimelineResponse, type ActivityGroup, type Category } from '@/lib/api';
import { useAuth } from '@/context/AuthContext';

// API 응답 타입이 any로 되어 있어, 실제 타입으로 명시
interface TimelineData extends TimelineResponse {
  timeBlocks: any[]; 
}

export function useDashboardData(selectedDate: Date) {
  const [timeBlocks, setTimeBlocks] = useState<any[]>([]);
  const [activities, setActivities] = useState<ActivityGroup[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);
  const { accessToken, isAuthenticated } = useAuth();

  const formatDate = (date: Date) => date.toISOString().split('T')[0];

  const fetchData = useCallback(async () => {
    if (!isAuthenticated || !accessToken) {
      if(!isLoading) setIsLoading(true);
      return;
    }

    const dateStr = formatDate(selectedDate);
    
    try {
      setIsLoading(true);
      setError(null);
      const [timelineData, activitiesData, categoriesData] = await Promise.all([
        api.getTimeline(accessToken, dateStr),
        api.getActivities(accessToken),
        api.getCategories(accessToken),
      ]);
      
      setTimeBlocks((timelineData as TimelineData)?.timeBlocks || []);
      setActivities(activitiesData || []);
      setCategories(categoriesData || []);

    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch dashboard data'));
      console.error(`[Dashboard] Fetching failed for date: ${dateStr}`, err);
      setTimeBlocks([]);
      setActivities([]);
      setCategories([]);
    } finally {
      setIsLoading(false);
    }
  }, [selectedDate, accessToken, isAuthenticated]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { timeBlocks, activities, categories, isLoading, error, refetch: fetchData };
}