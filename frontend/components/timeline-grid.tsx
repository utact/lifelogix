"use client"

import { useState, useRef, useEffect } from "react"
import { Card } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { ChevronLeft, ChevronRight, Calendar } from "lucide-react"
import { api, type TimeBlock, type ActivityGroup } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { TimeBlockCell } from "./time-block-cell"
import { BulkTimeBlockDialog } from "./bulk-time-block-dialog"

interface TimelineGridProps {
  timeBlocks: TimeBlock[];
  activities: ActivityGroup[];
  selectedDate: Date;
  onDateChange: (newDate: Date) => void;
  onRefresh: () => void;
  isLoading: boolean;
}

export function TimelineGrid({ 
  timeBlocks, 
  activities, 
  selectedDate, 
  onDateChange, 
  onRefresh,
  isLoading 
}: TimelineGridProps) {
  const { toast } = useToast()

  const [isDragging, setIsDragging] = useState(false)
  const [dragStart, setDragStart] = useState<number | null>(null)
  const [dragEnd, setDragEnd] = useState<number | null>(null)
  const [dragType, setDragType] = useState<"PLAN" | "ACTUAL" | null>(null)
  const [showBulkDialog, setShowBulkDialog] = useState(false)
  const timeSlotRefs = useRef<(HTMLDivElement | null)[]>([]);

  useEffect(() => {
    if (isToday) {
      const now = new Date();
      const currentHour = now.getHours();
      const currentMinute = now.getMinutes();
      const currentIndex = currentHour * 2 + Math.floor(currentMinute / 30);
      
      const targetRef = timeSlotRefs.current[currentIndex];
      if (targetRef) {
        targetRef.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }
  }, []); // Run only once on mount

  const formatDate = (date: Date) => {
    return date.toISOString().split("T")[0]
  }

  const changeDate = (days: number) => {
    const newDate = new Date(selectedDate)
    newDate.setDate(newDate.getDate() + days)
    onDateChange(newDate)
  }

  const goToToday = () => {
    onDateChange(new Date())
  }

  const generateTimeSlots = () => {
    const slots = []
    for (let hour = 0; hour < 24; hour++) {
      for (let minute = 0; minute < 60; minute += 30) {
        const timeStr = `${hour.toString().padStart(2, "0")}:${minute.toString().padStart(2, "0")}:00`
        const block = timeBlocks.find((b) => b.startTime === timeStr)
        slots.push({
          time: timeStr,
          displayTime: `${hour.toString().padStart(2, "0")}:${minute.toString().padStart(2, "0")}`,
          block: block || null,
        })
      }
    }
    return slots
  }

  const timeSlots = generateTimeSlots()

  const handleDragStart = (index: number, type: "PLAN" | "ACTUAL") => {
    setIsDragging(true)
    setDragStart(index)
    setDragEnd(index)
    setDragType(type)
  }

  const handleDragMove = (index: number) => {
    if (isDragging) {
      setDragEnd(index)
    }
  }

  const handleDragEnd = () => {
    if (isDragging && dragStart !== null && dragEnd !== null) {
      setIsDragging(false)
      setShowBulkDialog(true)
    }
  }

  const getSelectedRange = () => {
    if (dragStart === null || dragEnd === null) return []
    const start = Math.min(dragStart, dragEnd)
    const end = Math.max(dragStart, dragEnd)
    return timeSlots.slice(start, end + 1)
  }

  const handleBulkSave = async (activityId: number, activityName?: string, categoryId?: number) => {
    const selectedSlots = getSelectedRange()
    try {
      let finalActivityId = activityId
      if (activityName && categoryId) {
        const newActivity = await api.createActivity({
          name: activityName,
          categoryId,
        })
        finalActivityId = newActivity.id
      }

      await Promise.all(
        selectedSlots.map((slot) =>
          api.createTimeBlock({
            date: formatDate(selectedDate),
            startTime: slot.time,
            type: dragType!,
            activityId: finalActivityId,
          }),
        ),
      )
      toast({
        title: "일괄 저장 완료",
        description: `${selectedSlots.length}개의 타임 블록이 저장되었습니다`,
      })
      setShowBulkDialog(false)
      setDragStart(null)
      setDragEnd(null)
      setDragType(null)
      onRefresh()
    } catch (error) {
      toast({
        title: "저장 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    }
  }

  const isInSelectedRange = (index: number) => {
    if (!isDragging || dragStart === null || dragEnd === null) return false
    const start = Math.min(dragStart, dragEnd)
    const end = Math.max(dragStart, dragEnd)
    return index >= start && index <= end
  }

  const isPlanSelected = (index: number) => {
    return isInSelectedRange(index) && dragType === "PLAN"
  }

  const isActualSelected = (index: number) => {
    return isInSelectedRange(index) && dragType === "ACTUAL"
  }

  const isToday = formatDate(selectedDate) === formatDate(new Date())

  return (
    <div className="space-y-4">
      <Card className="p-4 bg-gradient-to-r from-card to-card/50 backdrop-blur-sm">
        <div className="flex items-center justify-between">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => changeDate(-1)}
            className="transition-all duration-200 hover:scale-110"
          >
            <ChevronLeft className="h-5 w-5" />
          </Button>
          <div className="flex flex-col items-center gap-2">
            <h2 className="text-2xl font-bold text-balance">
              {selectedDate.toLocaleDateString("ko-KR", {
                year: "numeric",
                month: "long",
                day: "numeric",
                weekday: "long",
              })}
            </h2>
            {!isToday && (
              <Button
                variant="outline"
                size="sm"
                onClick={goToToday}
                className="transition-all duration-200 hover:scale-105 bg-transparent"
              >
                <Calendar className="mr-2 h-4 w-4" />
                오늘로 이동
              </Button>
            )}
          </div>
          <Button
            variant="ghost"
            size="icon"
            onClick={() => changeDate(1)}
            className="transition-all duration-200 hover:scale-110"
          >
            <ChevronRight className="h-5 w-5" />
          </Button>
        </div>
      </Card>

      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <div className="animate-pulse text-muted-foreground">로딩 중...</div>
        </div>
      ) : (
        <div className="grid gap-2">
          {timeSlots.map((slot, index) => (
            <div key={slot.time} ref={(el) => (timeSlotRefs.current[index] = el)}>
              <TimeBlockCell
                time={slot.displayTime}
                timeBlock={slot.block}
                date={formatDate(selectedDate)}
                activities={activities}
                onUpdate={onRefresh}
                onDragStart={(type) => handleDragStart(index, type)}
                onDragMove={() => handleDragMove(index)}
                onDragEnd={handleDragEnd}
                isSelected={isInSelectedRange(index)}
                isPlanSelected={isPlanSelected(index)}
                isActualSelected={isActualSelected(index)}
                isDragging={isDragging}
              />
            </div>
          ))}
        </div>
      )}

      <BulkTimeBlockDialog
        open={showBulkDialog}
        onOpenChange={setShowBulkDialog}
        activities={activities}
        selectedRange={getSelectedRange()}
        type={dragType || "ACTUAL"}
        onSave={handleBulkSave}
      />
    </div>
  )
}