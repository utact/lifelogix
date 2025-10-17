"use client"

import { useState } from "react"
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { Sparkles } from "lucide-react"
import type { ActivityGroup, Category } from "@/lib/api"

interface BulkTimeBlockDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  activities: ActivityGroup[]
  categories: Category[]
  selectedRange: Array<{ time: string; displayTime: string }>
  type: "PLAN" | "ACTUAL"
  onSave: (activityId: number, activityName?: string, categoryId?: number) => void
}

export function BulkTimeBlockDialog({
  open,
  onOpenChange,
  activities,
  categories,
  selectedRange,
  type,
  onSave,
}: BulkTimeBlockDialogProps) {
  const [selectedActivityId, setSelectedActivityId] = useState<string>("")
  const [isCreatingNew, setIsCreatingNew] = useState(false)
  const [newActivityName, setNewActivityName] = useState("")
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>("")

  const handleSave = () => {
    if (isCreatingNew) {
      if (!newActivityName.trim() || !selectedCategoryId) {
        return
      }
      onSave(0, newActivityName, Number.parseInt(selectedCategoryId))
      setNewActivityName("")
      setSelectedCategoryId("")
      setIsCreatingNew(false)
    } else {
      if (selectedActivityId) {
        onSave(Number.parseInt(selectedActivityId))
        setSelectedActivityId("")
      }
    }
  }

  const startTime = selectedRange[0]?.displayTime
  const endTime = selectedRange[selectedRange.length - 1]?.displayTime

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[500px]">
        <DialogHeader>
          <DialogTitle>일괄 타임 블록 추가</DialogTitle>
          <DialogDescription>
            {startTime} ~ {endTime} ({selectedRange.length}개 블록)에 {type === "PLAN" ? "계획" : "실제 기록"}을
            추가합니다
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <Button
              type="button"
              variant={!isCreatingNew ? "default" : "outline"}
              size="sm"
              onClick={() => setIsCreatingNew(false)}
              className="flex-1 transition-all duration-200"
            >
              기존 활동 선택
            </Button>
            <Button
              type="button"
              variant={isCreatingNew ? "default" : "outline"}
              size="sm"
              onClick={() => setIsCreatingNew(true)}
              className="flex-1 transition-all duration-200"
            >
              <Sparkles className="mr-2 h-4 w-4" />새 활동 만들기
            </Button>
          </div>

          {!isCreatingNew ? (
            <div className="space-y-2">
              <Label>활동 선택</Label>
              <Select value={selectedActivityId} onValueChange={setSelectedActivityId}>
                <SelectTrigger>
                  <SelectValue placeholder="활동을 선택하세요" />
                </SelectTrigger>
                <SelectContent>
                  {activities.map((group) => (
                    <div key={group.categoryId}>
                      <div className="mx-1 my-1 rounded-sm bg-muted px-2 py-1.5 text-xs font-semibold text-muted-foreground">
                        {group.categoryName}
                      </div>
                      {group.activities.map((activity) => (
                        <SelectItem key={activity.id} value={activity.id.toString()} className="mx-1 w-[calc(100%-0.5rem)]">
                          {activity.name}
                        </SelectItem>
                      ))}
                    </div>
                  ))}
                </SelectContent>
              </Select>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="bulkNewActivityName">활동 이름</Label>
                <Input
                  id="bulkNewActivityName"
                  placeholder="예: LifeLogix 개발"
                  value={newActivityName}
                  onChange={(e) => setNewActivityName(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>카테고리</Label>
                <Select value={selectedCategoryId} onValueChange={setSelectedCategoryId}>
                  <SelectTrigger>
                    <SelectValue placeholder="카테고리를 선택하세요" />
                  </SelectTrigger>
                  <SelectContent>
                    {categories.map((category) => (
                      <SelectItem key={category.id} value={category.id.toString()}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}

          <div className="flex gap-2">
            <Button
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="flex-1 transition-all duration-200"
            >
              취소
            </Button>
            <Button
              onClick={handleSave}
              disabled={isCreatingNew ? !newActivityName.trim() || !selectedCategoryId : !selectedActivityId}
              className="flex-1 transition-all duration-200"
            >
              {isCreatingNew ? "활동 생성 및 저장" : "저장"}
            </Button>
          </div>
        </div>
      </DialogContent>
    </Dialog>
  )
}