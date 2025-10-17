"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { TimelineGrid } from "@/components/timeline-grid"
import { DailySummary } from "@/components/daily-summary"
import { DailyReflection } from "@/components/daily-reflection"
import { Button } from "@/components/ui/button"
import { FolderKanban, ListTodo } from "lucide-react"
import { useToast } from "@/hooks/use-toast"
import { useDashboardData } from "@/hooks/use-dashboard-data"

export default function DashboardPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [selectedDate, setSelectedDate] = useState(new Date())

  const { timeBlocks, activities, categories, isLoading, error, refetch } = useDashboardData(selectedDate)

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      router.push("/login")
    }
  }, [router])

  useEffect(() => {
    if (error) {
      toast({
        title: "데이터 로드 실패",
        description: error.message,
        variant: "destructive",
      })
    }
  }, [error, toast])

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <div className="container mx-auto max-w-7xl p-6 pb-10">
        <div className="mb-4 flex gap-2">
          <Button variant="outline" onClick={() => router.push("/categories")}>
            <FolderKanban className="mr-2 h-4 w-4" />
            카테고리 관리
          </Button>
          <Button variant="outline" onClick={() => router.push("/activities")}>
            <ListTodo className="mr-2 h-4 w-4" />
            활동 관리
          </Button>
        </div>
        <div className="grid grid-cols-1 lg:grid-cols-[1fr_320px] gap-4">
          <div className="space-y-4">
            <TimelineGrid 
              timeBlocks={timeBlocks}
              activities={activities}
              categories={categories}
              selectedDate={selectedDate}
              onDateChange={setSelectedDate}
              onRefresh={refetch}
              isLoading={isLoading}
            />
            <DailyReflection />
          </div>
          <div className="space-y-4 sticky top-20 self-start">
            <DailySummary timeBlocks={timeBlocks} activities={activities} />
          </div>
        </div>
      </div>
    </div>
  )
}