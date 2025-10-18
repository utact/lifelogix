"use client"

import type React from "react"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { CategoryActions } from "@/components/category-actions"
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
import { api, type Category } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { ArrowLeft, Plus } from "lucide-react"

export default function CategoriesPage() {
  const router = useRouter()
  const { toast } = useToast()
  const [categories, setCategories] = useState<Category[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isOpen, setIsOpen] = useState(false)
  const [formData, setFormData] = useState({
    name: "",
    color: "#5D6D7E",
    parentId: "",
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const loadCategories = async (token: string) => {
    try {
      setIsLoading(true)
      const data = await api.getCategories(token)
      setCategories(data)
    } catch (error) {
      toast({
        title: "카테고리 로드 실패",
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
    loadCategories(token)
  }, [router])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    const token = localStorage.getItem("accessToken")
    if (!token) {
      toast({ title: "인증 토큰이 없습니다. 다시 로그인해주세요.", variant: "destructive" })
      router.push("/login")
      return
    }

    if (!formData.parentId) {
      toast({
        title: "부모 카테고리를 선택해주세요",
        variant: "destructive",
      })
      return
    }

    setIsSubmitting(true)
    try {
      await api.createCategory(
        token,
        {
          name: formData.name,
          color: formData.color,
          parentId: Number.parseInt(formData.parentId),
        }
      )
      toast({
        title: "카테고리 생성 완료",
      })
      setIsOpen(false)
      setFormData({ name: "", color: "#5D6D7E", parentId: "" })
      loadCategories(token)
    } catch (error) {
      toast({
        title: "카테고리 생성 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const systemCategories = categories.filter((c) => !c.isCustom)
  const customCategories = categories.filter((c) => c.isCustom)

  const predefinedColors = ["#5D6D7E", "#9B59B6", "#3498DB", "#1ABC9C", "#F39C12", "#E74C3C", "#E91E63", "#2ECC71"]

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
              <h1 className="text-2xl font-bold">카테고리 관리</h1>
              <p className="text-sm text-muted-foreground">활동을 분류할 카테고리를 관리하세요</p>
            </div>
          </div>
          <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />새 카테고리
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>새 카테고리 만들기</DialogTitle>
                <DialogDescription>시스템 카테고리를 부모로 하는 커스텀 카테고리를 생성합니다</DialogDescription>
              </DialogHeader>
              <form onSubmit={handleSubmit} className="space-y-4">
                <div className="space-y-2">
                  <Label htmlFor="name">카테고리 이름</Label>
                  <Input
                    id="name"
                    value={formData.name}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    placeholder="예: 사이드 프로젝트"
                    required
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="parent">부모 카테고리</Label>
                  <Select
                    value={formData.parentId}
                    onValueChange={(value) => setFormData({ ...formData, parentId: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="시스템 카테고리 선택" />
                    </SelectTrigger>
                    <SelectContent>
                      {systemCategories.map((category) => (
                        <SelectItem key={category.id} value={category.id.toString()}>
                          <div className="flex items-center gap-2">
                            <div className="h-3 w-3 rounded-full" style={{ backgroundColor: category.color }} />
                            {category.name}
                          </div>
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="color">색상</Label>
                  <div className="flex gap-2">
                    {predefinedColors.map((color) => (
                      <button
                        key={color}
                        type="button"
                        className="h-8 w-8 rounded-md border-2 transition-all hover:scale-110"
                        style={{
                          backgroundColor: color,
                          borderColor: formData.color === color ? "white" : "transparent",
                        }}
                        onClick={() => setFormData({ ...formData, color })}
                      />
                    ))}
                  </div>
                  <Input
                    id="color"
                    type="color"
                    value={formData.color}
                    onChange={(e) => setFormData({ ...formData, color: e.target.value })}
                    className="h-10"
                  />
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
        ) : (
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <CardTitle>시스템 카테고리</CardTitle>
                <CardDescription>기본 제공되는 카테고리입니다</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                  {systemCategories.map((category) => (
                    <div key={category.id} className="flex items-center gap-3 rounded-lg border border-border p-3">
                      <div className="h-10 w-10 rounded-md" style={{ backgroundColor: category.color }} />
                      <div>
                        <div className="font-medium">{category.name}</div>
                        <div className="text-xs text-muted-foreground">시스템</div>
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>커스텀 카테고리</CardTitle>
                <CardDescription>사용자가 생성한 카테고리입니다</CardDescription>
              </CardHeader>
              <CardContent>
                {customCategories.length === 0 ? (
                  <div className="py-8 text-center text-muted-foreground">아직 커스텀 카테고리가 없습니다</div>
                ) : (
                  <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                    {customCategories.map((category) => {
                      const parent = categories.find((c) => c.id === category.parentId)
                      return (
                        <div key={category.id} className="flex items-center justify-between rounded-lg border border-border p-3">
                          <div className="flex items-center gap-3">
                            <div className="h-10 w-10 rounded-md" style={{ backgroundColor: category.color }} />
                            <div>
                              <div className="font-medium">{category.name}</div>
                              <div className="text-xs text-muted-foreground">{parent?.name} 하위</div>
                            </div>
                          </div>
                          <CategoryActions category={category} onSuccess={() => loadCategories(localStorage.getItem("accessToken")!)} />
                        </div>
                      )
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        )}
      </div>
    </div>
  )
}
