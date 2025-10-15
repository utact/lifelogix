"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Sparkles, Target, Calendar } from "lucide-react"

export function AIGoalPlanner() {
  return (
    <Card className="border-primary/20 dark:border-primary/30">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Sparkles className="h-5 w-5 text-primary" />
            <CardTitle>AI Goal Planner</CardTitle>
          </div>
          <Badge variant="secondary" className="gap-1">
            <Target className="h-3 w-3" />
            v2.0.0
          </Badge>
        </div>
        <CardDescription>AI가 당신의 목표를 제안합니다</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Button disabled className="w-full bg-transparent" variant="outline">
          <Sparkles className="mr-2 h-4 w-4" />
          목표 제안 받기
        </Button>

        <div className="rounded-lg border border-dashed border-primary/30 bg-primary/5 p-3">
          <div className="flex items-center gap-2 text-sm">
            <Calendar className="h-4 w-4 text-primary" />
            <span className="font-medium">주간/월간 목표 설정 기능 준비 중</span>
          </div>
        </div>
      </CardContent>
    </Card>
  )
}