"use client"

import type React from "react"
import { useState } from "react"
import { useRouter } from "next/navigation"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog"
import { Button } from "@/components/ui/button"
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
import { api, type Activity } from "@/lib/api"
import { useToast } from "@/hooks/use-toast"
import { Pencil, Trash2 } from "lucide-react"

interface ActivityActionsProps {
  activity: Activity
  onSuccess: (token: string) => void
}

export function ActivityActions({ activity, onSuccess }: ActivityActionsProps) {
  const router = useRouter()
  const { toast } = useToast()
  const [isOpen, setIsOpen] = useState(false)
  const [name, setName] = useState(activity.name)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault()
    const token = localStorage.getItem("accessToken")
    if (!token) {
      toast({ title: "인증 토큰이 없습니다. 다시 로그인해주세요.", variant: "destructive" })
      router.push("/login")
      return
    }

    setIsSubmitting(true)
    try {
      await api.updateActivity(token, activity.id, { name })
      toast({ title: "활동 수정 완료" })
      setIsOpen(false)
      onSuccess(token)
    } catch (error) {
      toast({
        title: "활동 수정 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDelete = async () => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      toast({ title: "인증 토큰이 없습니다. 다시 로그인해주세요.", variant: "destructive" })
      router.push("/login")
      return
    }

    try {
      await api.deleteActivity(token, activity.id)
      toast({ title: "활동 삭제 완료" })
      onSuccess(token)
    } catch (error) {
      toast({
        title: "활동 삭제 실패",
        description: error instanceof Error ? error.message : "다시 시도해주세요",
        variant: "destructive",
      })
    }
  }

  return (
    <div className="flex items-center gap-2">
      <Dialog open={isOpen} onOpenChange={setIsOpen}>
        <DialogTrigger asChild>
          <Button variant="ghost" size="icon">
            <Pencil className="h-4 w-4" />
          </Button>
        </DialogTrigger>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>활동 수정</DialogTitle>
            <DialogDescription>활동의 이름을 변경합니다.</DialogDescription>
          </DialogHeader>
          <form onSubmit={handleUpdate} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">활동 이름</Label>
              <Input
                id="name"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="예: LifeLogix 개발"
                required
              />
            </div>
            <Button type="submit" disabled={isSubmitting} className="w-full">
              {isSubmitting ? "수정 중..." : "수정"}
            </Button>
          </form>
        </DialogContent>
      </Dialog>

      <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button variant="ghost" size="icon" className="text-destructive hover:text-destructive">
            <Trash2 className="h-4 w-4" />
          </Button>
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>정말 삭제하시겠습니까?</AlertDialogTitle>
            <AlertDialogDescription>이 작업은 되돌릴 수 없습니다. 활동을 삭제하면 관련 데이터가 모두 삭제될 수 있습니다.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>취소</AlertDialogCancel>
            <AlertDialogAction onClick={handleDelete}>삭제</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
