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
    try {
      setIsLoading(true);
      setError(null);
      const dateStr = formatDate(selectedDate);
      const [timelineData, activitiesData] = await Promise.all([
        api.getTimeline(dateStr),
        api.getActivities(),
      ]);
      setTimeBlocks(timelineData.timeBlocks);
      setActivities(activitiesData);
    } catch (err) {
      setError(err instanceof Error ? err : new Error('Failed to fetch data'));
    } finally {
      setIsLoading(false);
    }
  }, [selectedDate]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  return { timeBlocks, activities, isLoading, error, refetch: fetchData };
}