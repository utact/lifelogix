"use client"

import { useState } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Textarea } from "@/components/ui/textarea"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { BookOpen, Save, Sparkles } from "lucide-react"
import { useToast } from "@/hooks/use-toast"

export function DailyReflection() {
  const { toast } = useToast()
  const [reflection, setReflection] = useState("")
  const [isSaving, setIsSaving] = useState(false)
  const [savedReflection, setSavedReflection] = useState("")

  const handleSave = async () => {
    if (!reflection.trim()) {
      toast({
        title: "회고 내용을 입력해주세요",
        variant: "destructive",
      })
      return
    }

    setIsSaving(true)
    // Simulate API call
    await new Promise((resolve) => setTimeout(resolve, 500))
    setSavedReflection(reflection)
    toast({
      title: "회고가 저장되었습니다",
      description: "오늘의 회고가 성공적으로 기록되었습니다",
    })
    setIsSaving(false)
  }

  return (
    <Card className="bg-gradient-to-br from-card to-card/50 backdrop-blur-sm">
      <CardHeader>
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2">
            <BookOpen className="h-5 w-5 text-primary" />
            <CardTitle>하루 회고</CardTitle>
          </div>
        </div>
        <CardDescription>오늘 하루를 돌아보고 기록해보세요</CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <Textarea
          placeholder="오늘 하루는 어땠나요? 잘한 점, 개선할 점, 내일의 계획 등을 자유롭게 작성해보세요..."
          value={reflection}
          onChange={(e) => setReflection(e.target.value)}
          className="min-h-[120px] resize-none"
        />
        <Button onClick={handleSave} disabled={isSaving} className="w-full">
          <Save className="mr-2 h-4 w-4" />
          {isSaving ? "저장 중..." : "회고 저장"}
        </Button>
        {savedReflection && (
          <div className="rounded-lg border border-primary/20 bg-primary/5 p-3">
            <p className="text-xs text-muted-foreground mb-1">저장된 회고</p>
            <p className="text-sm">{savedReflection}</p>
          </div>
        )}
      </CardContent>
    </Card>
  )
}
