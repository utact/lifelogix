"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { DashboardHeader } from "@/components/dashboard-header"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Input } from "@/components/ui/input"
import { Users, UserPlus, Award, Search } from "lucide-react"

export default function SocialPage() {
  const router = useRouter()
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [friendSearchTerm, setFriendSearchTerm] = useState("")
  const [leaderboardSearchTerm, setLeaderboardSearchTerm] = useState("")

  useEffect(() => {
    const token = localStorage.getItem("accessToken")
    if (!token) {
      router.push("/login")
    } else {
      setIsAuthenticated(true)
    }
  }, [router])

  // Mock data is now empty
  const allFriends: any[] = []

  const filteredFriends = allFriends.filter(friend => 
    friend.name.toLowerCase().includes(friendSearchTerm.toLowerCase())
  );

  const sortedFriends = [...allFriends].sort((a, b) => b.focusScore - a.focusScore);

  const filteredLeaderboard = sortedFriends.filter(friend => 
    friend.name.toLowerCase().includes(leaderboardSearchTerm.toLowerCase())
  );

  if (!isAuthenticated) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <div className="text-muted-foreground">Loading...</div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <div className="container mx-auto max-w-7xl p-6">
        <div className="mb-6">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold flex items-center gap-2">
                <Users className="h-8 w-8 text-primary" />
                소셜
              </h1>
              <p className="text-muted-foreground">친구들과 함께 성장하세요</p>
            </div>
          </div>
        </div>

        <div className="grid gap-6 lg:grid-cols-2">
          <div className="lg:col-span-2 grid lg:grid-cols-2 gap-6 items-start">
            <Card className="flex flex-col">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Users className="h-5 w-5" />
                  친구 목록
                </CardTitle>
                <CardDescription>함께 성장하는 친구들</CardDescription>
              </CardHeader>
              <CardContent className="flex-grow flex flex-col gap-4">
                {allFriends.length > 0 ? (
                  <>
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                      <Input 
                        placeholder="친구 검색..." 
                        className="pl-10" 
                        value={friendSearchTerm}
                        onChange={(e) => setFriendSearchTerm(e.target.value)}
                      />
                    </div>
                    <div className="overflow-y-auto pr-2 space-y-4 h-[280px]">
                      {filteredFriends.map((friend) => (
                        <div
                          key={friend.id}
                          className="flex items-center justify-between p-3 rounded-lg border border-border hover:bg-muted/50 transition-colors"
                        >
                          <div className="flex items-center gap-3">
                            <Avatar>
                              <AvatarFallback>{friend.name[0]}</AvatarFallback>
                            </Avatar>
                            <div>
                              <div className="font-medium">{friend.name}</div>
                              <div className="text-xs text-muted-foreground">
                                레벨 {friend.level} · {friend.streak}일 연속
                              </div>
                            </div>
                          </div>
                          <div className="text-right">
                            <div className="text-sm font-bold text-primary">{friend.focusScore}</div>
                            <div className="text-xs text-muted-foreground">집중도</div>
                          </div>
                        </div>
                      ))}
                    </div>
                  </>
                ) : (
                  <div className="flex-grow flex items-center justify-center h-[280px]">
                    <p className="text-muted-foreground">등록된 친구가 없습니다.</p>
                  </div>
                )}
              </CardContent>
            </Card>

            <Card className="flex flex-col">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Award className="h-5 w-5" />
                  리더보드
                </CardTitle>
                <CardDescription>친구들의 집중도 순위</CardDescription>
              </CardHeader>
              <CardContent className="flex-grow flex flex-col gap-4">
                {allFriends.length > 0 ? (
                  <>
                    <div className="relative">
                      <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
                      <Input 
                        placeholder="순위 검색..." 
                        className="pl-10" 
                        value={leaderboardSearchTerm}
                        onChange={(e) => setLeaderboardSearchTerm(e.target.value)}
                      />
                    </div>
                    <div className="overflow-y-auto pr-2 space-y-4 h-[280px]">
                      {filteredLeaderboard.map((friend, index) => {
                        const rank = sortedFriends.findIndex(f => f.id === friend.id) + 1;
                        const medal = rank === 1 ? "🥇" : rank === 2 ? "🥈" : rank === 3 ? "🥉" : null;
                        return (
                          <div key={friend.id} className="flex items-center gap-3 p-3 rounded-lg border border-border">
                            <div className="flex items-center justify-center w-8 text-lg font-bold">
                              {medal ? <span className="text-2xl">{medal}</span> : <span>{rank}</span>}
                            </div>
                            <div className="flex-1">
                              <div className="font-medium">{friend.name}</div>
                              <div className="text-xs text-muted-foreground">집중도 {friend.focusScore}점</div>
                            </div>
                            <Badge variant="secondary">#{rank}</Badge>
                          </div>
                        )
                      })}
                    </div>
                  </>
                ) : (
                  <div className="flex-grow flex items-center justify-center h-[280px]">
                    <p className="text-muted-foreground">등록된 친구가 없습니다.</p>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          <div className="lg:col-span-2">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <UserPlus className="h-5 w-5" />
                  친구 추가
                </CardTitle>
                <CardDescription>이메일로 친구를 초대하세요</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex gap-2">
                  <input
                    type="email"
                    placeholder="friend@example.com"
                    className="flex-1 px-3 py-2 rounded-md border border-border bg-background"
                  />
                  <Button>초대하기</Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  )
}