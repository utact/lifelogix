"use client"

import { useState } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Label } from "@/components/ui/label"
import { Input } from "@/components/ui/input"
import { api, type TimeBlock, type ActivityGroup, type Category } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { Check, Sparkles, Loader2 } from "lucide-react"
import { cn } from "@/lib/utils"
import { ActivityCombobox } from "./activity-combobox"
import { CategoryCombobox } from "./category-combobox"

interface TimeBlockCellProps {
  time: string
  timeBlock: TimeBlock | null
  date: string
  activities: ActivityGroup[]
  categories: Category[]
  token: string | null;
  onUpdate: () => Promise<void>
  onDragStart: (type: "PLAN" | "ACTUAL") => void
  onDragMove: () => void
  onDragEnd: () => void
  isSelected: boolean
  isPlanSelected: boolean
  isActualSelected: boolean
  isDragging: boolean
}

export function TimeBlockCell({
  time,
  timeBlock,
  date,
  activities,
  categories,
  token,
  onUpdate,
  onDragStart,
  onDragMove,
  onDragEnd,
  isSelected,
  isPlanSelected,
  isActualSelected,
  isDragging,
}: TimeBlockCellProps) {
  const { toast } = useToast()
  const [isOpen, setIsOpen] = useState(false)
  const [selectedActivityId, setSelectedActivityId] = useState<string>("")
  const [blockType, setBlockType] = useState<"PLAN" | "ACTUAL">("ACTUAL")
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isCreatingNew, setIsCreatingNew] = useState(false)
  const [newActivityName, setNewActivityName] = useState("")
  const [selectedCategoryId, setSelectedCategoryId] = useState<string>("")
  const [isCopying, setIsCopying] = useState(false)

  const handleCopyPlanToActual = async () => {
    if (!token) return; 

    if (!timeBlock?.plan) {
      toast({
        title: "계획이 없습니다",
        description: "먼저 계획을 추가해주세요",
        variant: "destructive",
      })
      return
    }

    setIsCopying(true)
    try {
      await api.createTimeBlock(token, {
        date,
        startTime: time,
        type: "ACTUAL",
        activityId: timeBlock.plan.activityId,
      })
      await onUpdate()
      toast({
        title: "실제 기록 완료",
        description: "계획한 대로 실행하셨네요! 🎉",
      })
    } catch (error) {
      toast({
        title: "저장 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsCopying(false)
    }
  }

  const handleSubmit = async () => {
    if (!token) return;

    if (!selectedActivityId && !isCreatingNew) {
      toast({
        title: "활동을 선택해주세요",
        variant: "destructive",
      })
      return
    }

    if (isCreatingNew) {
      if (!newActivityName.trim() || !selectedCategoryId) {
        toast({
            title: "활동 이름과 카테고리를 모두 선택해주세요",
            variant: "destructive",
        });
        return;
      }

      setIsSubmitting(true)
      try {
        const categoryId = Number.parseInt(selectedCategoryId);
        const newActivity = await api.createActivity(token, {
          name: newActivityName,
          categoryId,
        })

        await api.createTimeBlock(token, {
          date,
          startTime: time,
          type: blockType,
          activityId: newActivity.id,
        })

        toast({
          title: "활동 생성 및 타임 블록 저장 완료",
          description: `"${newActivityName}" 활동이 추가되었습니다`,
        })
        setIsOpen(false)
        setNewActivityName("")
        setSelectedCategoryId("")
        setIsCreatingNew(false)
        await onUpdate()
      } catch (error) {
        toast({
          title: "저장 실패",
          description: error instanceof Error ? error.message : "다시 시도해주세요",
          variant: "destructive",
        })
      } finally {
        setIsSubmitting(false)
      }
      return
    }

    setIsSubmitting(true)
    try {
      await api.createTimeBlock(token, {
        date,
        startTime: time,
        type: blockType,
        activityId: Number.parseInt(selectedActivityId),
      })
      toast({
        title: "타임 블록 저장 완료",
      })
      setIsOpen(false)
      await onUpdate()
    } catch (error) {
      toast({
        title: "저장 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <Card className="p-1.5 transition-all duration-200 hover:shadow-md min-h-[32px]">
      <div className="flex items-center gap-2">
        <div className="w-10 font-mono text-[10px] font-medium text-muted-foreground shrink-0">{time}</div>

        <div
          className={cn(
            "flex-1 cursor-pointer select-none transition-all duration-150 rounded-md min-h-[28px] flex items-center gap-1",
            isPlanSelected && "ring-2 ring-blue-500 bg-blue-50 dark:bg-blue-500/10 scale-[0.98]",
            !isPlanSelected && isDragging && "opacity-50",
          )}
          onMouseDown={() => onDragStart("PLAN")}
          onMouseEnter={onDragMove}
          onMouseUp={onDragEnd}
        >
          {timeBlock?.plan ? (
            <>
              <div
                className="w-[30%] rounded-md px-2 py-1 text-[10px] font-medium text-center transition-all duration-200 hover:scale-[1.02]"
                style={{
                  backgroundColor: `${timeBlock.plan.categoryColor}30`,
                  borderLeft: `3px solid ${timeBlock.plan.categoryColor}`,
                }}
              >
                {timeBlock.plan.categoryName}
              </div>
              <div
                className="w-[70%] rounded-md px-2 py-1 text-[10px] font-medium text-center transition-all duration-200 hover:scale-[1.02]"
                style={{
                  backgroundColor: `${timeBlock.plan.categoryColor}40`,
                }}
              >
                {timeBlock.plan.activityName}
              </div>
            </>
          ) : (
            <div className="flex-1 flex items-center justify-center rounded-md border border-dashed border-border text-[9px] text-muted-foreground transition-all duration-200 hover:border-primary/50 hover:bg-primary/5 h-full">
              계획
            </div>
          )}
        </div>

        <div
          className={cn(
            "flex-1 cursor-pointer select-none transition-all duration-150 rounded-md min-h-[28px] flex items-center gap-1",
            isActualSelected && "ring-2 ring-green-500 bg-green-50 dark:bg-green-500/10 scale-[0.98]",
            !isActualSelected && isDragging && "opacity-50",
          )}
          onMouseDown={() => onDragStart("ACTUAL")}
          onMouseEnter={onDragMove}
          onMouseUp={onDragEnd}
        >
          {timeBlock?.actual ? (
            <>
              <div
                className="w-[70%] rounded-md px-2 py-1 text-[10px] font-medium text-center transition-all duration-200 hover:scale-[1.02]"
                style={{
                  backgroundColor: `${timeBlock.actual.categoryColor}40`,
                }}
              >
                {timeBlock.actual.activityName}
              </div>
              <div
                className="w-[30%] rounded-md px-2 py-1 text-[10px] font-medium text-center transition-all duration-200 hover:scale-[1.02]"
                style={{
                  backgroundColor: `${timeBlock.actual.categoryColor}30`,
                  borderRight: `3px solid ${timeBlock.actual.categoryColor}`,
                }}
              >
                {timeBlock.actual.categoryName}
              </div>
            </>
          ) : (
            <div className="flex-1 flex items-center justify-center rounded-md border border-dashed border-border text-[9px] text-muted-foreground transition-all duration-200 hover:border-primary/50 hover:bg-primary/5 h-full">
              실제
            </div>
          )}
        </div>

        {timeBlock?.plan && !timeBlock?.actual ? (
          <Button
            size="icon"
            variant="ghost"
            className="h-6 w-6 shrink-0 transition-all duration-200 hover:scale-110 hover:bg-green-100 dark:hover:bg-green-900/30"
            onClick={handleCopyPlanToActual}
            disabled={isCopying}
            title="계획대로 실행했어요"
          >
            {isCopying ? (
              <Loader2 className="h-3 w-3 animate-spin text-green-600 dark:text-green-400" />
            ) : (
              <Check className="h-3 w-3 text-green-600 dark:text-green-400" />
            )}
          </Button>
        ) : (
          <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogTrigger asChild>
              <div className="h-6 w-6 shrink-0" />
            </DialogTrigger>
            <DialogContent className="sm:max-w-[500px]">
              <DialogHeader>
                <DialogTitle>{time} 타임 블록 추가</DialogTitle>
                <DialogDescription>활동을 선택하거나 새로 만들어 계획 또는 실제 기록을 추가하세요</DialogDescription>
              </DialogHeader>
              <div className="space-y-4">
                <div className="space-y-2">
                  <Label>타입</Label>
                  <Select value={blockType} onValueChange={(value) => setBlockType(value as "PLAN" | "ACTUAL")}>
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="PLAN">계획</SelectItem>
                      <SelectItem value="ACTUAL">실제</SelectItem>
                    </SelectContent>
                  </Select>
                </div>

                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant={!isCreatingNew ? "default" : "outline"}
                    size="sm"
                    onClick={() => setIsCreatingNew(false)}
                    className="flex-1"
                  >
                    기존 활동 선택
                  </Button>
                  <Button
                    type="button"
                    variant={isCreatingNew ? "default" : "outline"}
                    size="sm"
                    onClick={() => setIsCreatingNew(true)}
                    className="flex-1"
                  >
                    <Sparkles className="mr-2 h-4 w-4" />새 활동 만들기
                  </Button>
                </div>

                {!isCreatingNew ? (
                  <div className="space-y-2">
                    <Label>활동</Label>
                    <ActivityCombobox
                      activityGroups={activities}
                      categories={categories}
                      value={selectedActivityId}
                      onValueChange={setSelectedActivityId}
                    />
                  </div>
                ) : (
                  <div className="space-y-4">
                    <div className="space-y-2">
                      <Label htmlFor="newActivityName">활동 이름</Label>
                      <Input
                        id="newActivityName"
                        placeholder="예: LifeLogix 개발"
                        value={newActivityName}
                        onChange={(e) => setNewActivityName(e.target.value)}
                      />
                    </div>
                    <div className="space-y-2">
                      <Label>카테고리</Label>
                      <CategoryCombobox
                        categories={categories}
                        value={selectedCategoryId}
                        onValueChange={setSelectedCategoryId}
                      />
                    </div>
                  </div>
                )}

                <Button onClick={handleSubmit} disabled={isSubmitting} className="w-full">
                  {isSubmitting ? "저장 중..." : isCreatingNew ? "활동 생성 및 저장" : "저장"}
                </Button>
              </div>
            </DialogContent>
          </Dialog>
        )}
      </div>
    </Card>
  )
}
