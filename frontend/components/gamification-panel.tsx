"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Progress } from "@/components/ui/progress"
import { Trophy, Star, Zap, Award, Lock } from "lucide-react"

export function GamificationPanel() {
  const missions = [
    {
      id: 1,
      title: "최초의 타임라인",
      description: "첫 타임라인을 등록하세요",
      progress: 0,
      icon: <Zap className="h-5 w-5 text-yellow-500" />,
    },
    {
      id: 2,
      title: "완벽한 하루",
      description: "모든 계획된 시간을 완료하세요",
      progress: 0,
      icon: <Star className="h-5 w-5 text-blue-500" />,
    },
    {
      id: 3,
      title: "계획적인 삶",
      description: "7일간 타임라인을 등록하세요",
      progress: 0,
      icon: <Trophy className="h-5 w-5 text-green-500" />,
    },
    {
      id: 4,
      title: "히든 미션",
      description: "???",
      progress: 0,
      icon: <Lock className="h-5 w-5 text-muted-foreground" />,
      hidden: true,
    },
  ]

  const currentLevel = 0
  const currentXP = 0
  const nextLevelXP = 1000
  const xpProgress = (currentXP / nextLevelXP) * 100

  return (
    <Card className="border-primary/20 dark:border-primary/30">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <Trophy className="h-5 w-5 text-primary" />
            <CardTitle>게이미피케이션</CardTitle>
          </div>
        </div>
        <CardDescription>레벨업하고 미션을 달성하세요</CardDescription>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* Level Progress */}
        <div className="space-y-2">
          <div className="flex items-center justify-between text-sm">
            <div className="flex items-center gap-2">
              <Star className="h-5 w-5 text-yellow-400" />
              <span className="font-bold">레벨 {currentLevel}</span>
            </div>
            <span className="font-mono text-muted-foreground">
              {currentXP} / {nextLevelXP} XP
            </span>
          </div>
          <Progress value={xpProgress} className="h-2" />
        </div>

        {/* Missions */}
        <div className="space-y-3">
          <h4 className="text-sm font-semibold flex items-center gap-2">
            <Award className="h-4 w-4" />
            오늘의 미션
          </h4>
          <div className="grid grid-cols-2 gap-4">
            {missions.map((mission) => (
              <div
                key={mission.id}
                className={`rounded-lg p-4 flex flex-col justify-between h-32 ${
                  mission.hidden
                    ? "bg-muted/50 border-2 border-dashed"
                    : "bg-card border"
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="space-y-1">
                    <p className="text-sm font-semibold">{mission.title}</p>
                    <p className="text-xs text-muted-foreground">
                      {mission.description}
                    </p>
                  </div>
                  {mission.icon}
                </div>
                <div className="flex items-center gap-2">
                  <Progress
                    value={mission.progress}
                    className="h-1.5 flex-1"
                  />
                  <span className="text-xs font-mono text-muted-foreground">
                    {mission.progress}%
                  </span>
                </div>
              </div>
            ))}
          </div>
        </div>
      </CardContent>
    </Card>
  )
}