"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { ActivityActions } from "@/components/activity-actions"
import { DashboardHeader } from "@/components/dashboard-header"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { api, type ActivityGroup, type Category } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { ArrowLeft, Plus } from "lucide-react"

export default function ActivitiesPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [activityGroups, setActivityGroups] = useState<ActivityGroup[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isOpen, setIsOpen] = useState(false)
  const [formData, setFormData] = useState({
    name: "",
    categoryId: "",
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadData = async (token: string) => {
    try {
      setIsLoading(true)
      const [activitiesData, categoriesData] = await Promise.all([
        api.getActivities(token),
        api.getCategories(token),
      ])
      setActivityGroups(activitiesData)
      setCategories(categoriesData)
    } catch (error) {
      toast({
        title: "데이터 로드 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      router.push("/login")
      return
    }
    loadData(token)
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const token = localStorage.getItem("accessToken")
    if (!token) {
      toast({ title: "인증 토큰이 없습니다. 다시 로그인해주세요.", variant: "destructive" })
      router.push("/login")
      return
    }

    if (!formData.categoryId) {
      toast({
        title: "카테고리를 선택해주세요",
        variant: "destructive",
      })
      return
    }

    setIsSubmitting(true)
    try {
      await api.createActivity(token, {
        name: formData.name,
        categoryId: Number.parseInt(formData.categoryId),
      })
      toast({
        title: "활동 생성 완료",
      })
      setIsOpen(false)
      setFormData({ name: "", categoryId: "" })
      loadData(token)
    } catch (error) {
      toast({
        title: "활동 생성 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <div className="container mx-auto p-4">
        <div className="mb-6 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <Button variant="ghost" size="icon" onClick={() => router.push("/dashboard")}>
              <ArrowLeft className="h-5 w-5" />
            </Button>
            <div>
              <h1 className="text-2xl font-bold">활동 관리</h1>
              <p className="text-sm text-muted-foreground">타임라인에 기록할 활동을 관리하세요</p>
            </div>
          </div>
          <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />새 활동
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>새 활동 만들기</DialogTitle>
                <DialogDescription>카테고리에 속하는 활동을 생성합니다</DialogDescription>
              </DialogHeader>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">활동 이름</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="예: LifeLogix 개발"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="category">카테고리</Label>
                  <Select
                    value={formData.categoryId}
                    onValueChange={(value) => setFormData({ ...formData, categoryId: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="카테고리 선택" />
                    </SelectTrigger>
                    <SelectContent>
                      {categories.map((category) => (
                        <SelectItem key={category.id} value={category.id.toString()}>
                          <div className="flex items-center gap-2">
                            <div className="h-3 w-3 rounded-full" style={{ backgroundColor: category.color }} />
                            {category.name}
                            {category.isCustom && <span className="text-xs text-muted-foreground">(커스텀)</span>}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <Button type="submit" disabled={isSubmitting} className="w-full">
                  {isSubmitting ? "생성 중..." : "생성"}
                </Button>
              </form>
            </DialogContent>
          </Dialog>
        </div>

        {isLoading ? (
          <div className="flex items-center justify-center py-12">
            <div className="text-muted-foreground">로딩 중...</div>
          </div>
        ) : activityGroups.length === 0 ? (
          <Card>
            <CardContent className="py-12 text-center">
              <p className="text-muted-foreground">아직 활동이 없습니다</p>
              <p className="mt-2 text-sm text-muted-foreground">새 활동 버튼을 눌러 첫 활동을 만들어보세요</p>
            </CardContent>
          </Card>
        ) : (
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            {activityGroups.map((group) => {
              const category = categories.find((c) => c.id === group.categoryId)
              return (
                <Card key={group.categoryId}>
                  <CardHeader>
                    <div className="flex items-center gap-2">
                      <div className="h-4 w-4 rounded-full" style={{ backgroundColor: category?.color || "#5D6D7E" }} />
                      <CardTitle className="text-lg">{group.categoryName}</CardTitle>
                    </div>
                    <CardDescription>{group.activities.length}개의 활동</CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      {group.activities.map((activity) => (
                        <div
                          key={activity.id}
                          className="flex items-center justify-between rounded-md border border-border bg-card px-3 py-2 text-sm transition-colors hover:bg-accent"
                        >
                          <span>{activity.name}</span>
                          <ActivityActions activity={activity} onSuccess={() => loadData(localStorage.getItem("accessToken")!)} />
                        </div>
                      ))}
                    </div>
                  </CardContent>
                </Card>
              )
            })}
          </div>
        )}
      </div>
    </div>
  )
}
