"use client"

import { useState, useEffect, useCallback } from 'react';
import { api, type TimeBlock, type ActivityGroup } from '@/lib/api';

export function useDashboardData(selectedDate: Date) {
  const [timeBlocks, setTimeBlocks] = useState<TimeBlock[]>([]);
  const [activities, setActivities] = useState<ActivityGroup[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  const formatDate = (date: Date) => {
    return date.toISOString().split('T')[0];
  };

  const fetchData = useCallback(async () => {
    const dateStr = formatDate(selectedDate);
    console.log(`[Frontend|useDashboardData] Fetching data - Attempt for date: ${dateStr}`);
    try {
      setIsLoading(true);
      setError(null);
      const [timelineData, activitiesData] = await Promise.all([
        api.getTimeline(dateStr),
        api.getActivities(),
      ]);
      setTimeBlocks(timelineData.timeBlocks);
      setActivities(activitiesData);
      console.log(`[Frontend|useDashboardData] Fetching data - Success for date: ${dateStr}`);
    } catch (err) {
      const error = err instanceof Error ? err : new Error('Failed to fetch data');
      console.error(`[Frontend|useDashboardData] Fetching data - Failed for date: ${dateStr}`, error);
      setError(error);
    } finally {
      setIsLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { timeBlocks, activities, isLoading, error, refetch: fetchData };
}