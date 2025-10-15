"use client"

import type React from "react"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import { Clock, Target, TrendingUp, Zap } from "lucide-react"
import type { TimeBlock, ActivityGroup } from "@/lib/api"
import { useMemo } from "react"
import { GOAL_HOURS } from "@/lib/config"

interface DailySummaryProps {
  timeBlocks: TimeBlock[]
  activities: ActivityGroup[]
}

export function DailySummary({ timeBlocks, activities }: DailySummaryProps) {
  const stats = useMemo(() => {
    const filledBlocks = timeBlocks.filter((b) => b.actual !== null)
    const totalBlocks = filledBlocks.length
    const totalHours = totalBlocks * 0.5 // Each block is 30 minutes

    // Calculate category breakdown
    const categoryMap = new Map<string, { hours: number; color: string }>()
    filledBlocks.forEach((block) => {
      if (block.actual) {
        const existing = categoryMap.get(block.actual.categoryName) || { hours: 0, color: block.actual.categoryColor }
        existing.hours += 0.5
        categoryMap.set(block.actual.categoryName, existing)
      }
    })

    const categories = Array.from(categoryMap.entries())
      .map(([name, data]) => ({
        name,
        hours: data.hours,
        color: data.color,
        percentage: totalHours > 0 ? (data.hours / totalHours) * 100 : 0,
      }))
      .sort((a, b) => b.hours - a.hours)
      .slice(0, 4)

    // Calculate focus score (plan vs actual match)
    const planBlocks = timeBlocks.filter((b) => b.plan !== null)
    const matchingBlocks = timeBlocks.filter(
      (b) => b.plan !== null && b.actual !== null && b.plan.activityId === b.actual.activityId,
    )
    const focusScore = planBlocks.length > 0 ? Math.round((matchingBlocks.length / planBlocks.length) * 100) : 0

    // Count unique categories and activities
    const uniqueCategories = new Set(activities.map((g) => g.categoryId)).size
    const totalActivities = activities.reduce((sum, g) => sum + g.activities.length, 0)

    return {
      totalHours,
      goalHours: GOAL_HOURS,
      focusScore,
      categories,
      uniqueCategories,
      totalActivities,
      totalBlocks,
    }
  }, [timeBlocks, activities])

  const progressPercentage = (stats.totalHours / stats.goalHours) * 100

  return (
    <div className="space-y-4">
      {/* Today's Progress */}
      <Card className="bg-card/80 backdrop-blur-sm border-border/50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <Target className="h-5 w-5 text-primary" />
            오늘의 진행률
          </CardTitle>
          <CardDescription>목표 대비 현재 진행 상황</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">진행 시간</span>
              <span className="font-bold">
                {stats.totalHours.toFixed(1)}h / {stats.goalHours}h
              </span>
            </div>
            <Progress value={progressPercentage} className="h-3" />
            <div className="text-right text-xs text-muted-foreground">{progressPercentage.toFixed(0)}% 달성</div>
          </div>
        </CardContent>
      </Card>

      {/* Focus Score */}
      <Card className="bg-card/80 backdrop-blur-sm border-primary/20">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <Zap className="h-5 w-5 text-primary" />
            집중도 점수
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div className="text-4xl font-bold text-primary">{stats.focusScore}</div>
            <Badge variant="secondary" className="gap-1">
              <TrendingUp className="h-3 w-3" />
              실시간 계산
            </Badge>
          </div>
          <p className="mt-2 text-xs text-muted-foreground">계획 대비 실제 활동의 일치도를 기반으로 계산됩니다</p>
        </CardContent>
      </Card>

      {/* Category Breakdown */}
      <Card className="bg-card/80 backdrop-blur-sm border-border/50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg">
            <Clock className="h-5 w-5 text-primary" />
            카테고리별 시간
          </CardTitle>
          <CardDescription>오늘 활동한 카테고리 분포</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          {stats.categories.length > 0 ? (
            stats.categories.map((category) => (
              <div key={category.name} className="space-y-1.5">
                <div className="flex items-center justify-between text-sm">
                  <div className="flex items-center gap-2">
                    <div className="h-3 w-3 rounded-full" style={{ backgroundColor: category.color }} />
                    <span className="font-medium">{category.name}</span>
                  </div>
                  <span className="text-muted-foreground">{category.hours.toFixed(1)}h</span>
                </div>
                <Progress
                  value={category.percentage}
                  className="h-1.5"
                  style={
                    {
                      "--progress-background": category.color,
                    } as React.CSSProperties
                  }
                />
              </div>
            ))
          ) : (
            <p className="text-sm text-muted-foreground text-center py-4">아직 기록된 활동이 없습니다</p>
          )}
        </CardContent>
      </Card>

      <Card className="bg-card/80 backdrop-blur-sm border-border/50">
        <CardHeader>
          <CardTitle className="text-lg">빠른 통계</CardTitle>
        </CardHeader>
        <CardContent className="grid grid-cols-3 gap-3">
          <div className="rounded-lg border border-border bg-card p-3 text-center">
            <div className="text-2xl font-bold text-foreground">{stats.uniqueCategories}</div>
            <div className="text-xs text-muted-foreground whitespace-nowrap mt-1">총 카테고리</div>
          </div>
          <div className="rounded-lg border border-border bg-card p-3 text-center">
            <div className="text-2xl font-bold text-foreground">{stats.totalActivities}</div>
            <div className="text-xs text-muted-foreground whitespace-nowrap mt-1">총 활동</div>
          </div>
          <div className="rounded-lg border border-border bg-card p-3 text-center">
            <div className="text-2xl font-bold text-foreground">{stats.totalBlocks}</div>
            <div className="text-xs text-muted-foreground whitespace-nowrap mt-1">기록된 블록</div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
