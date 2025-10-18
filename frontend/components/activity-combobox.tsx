"use client"

import * as React from "react"
import { Check, ChevronsUpDown } from "lucide-react"

import { cn } from "@/lib/utils"
import { Button } from "@/components/ui/button"
import {
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
} from "@/components/ui/command"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import type { ActivityGroup, Category } from "@/lib/api"

interface ActivityComboboxProps {
  activityGroups: ActivityGroup[]
  categories: Category[]
  value: string
  onValueChange: (value: string) => void
}

export function ActivityCombobox({ activityGroups, categories, value, onValueChange }: ActivityComboboxProps) {
  const [open, setOpen] = React.useState(false)

  const allActivities = React.useMemo(() => {
    return activityGroups.flatMap((group) => {
      const category = categories.find((c) => c.id === group.categoryId)
      return group.activities.map((activity) => ({
        ...activity,
        categoryId: group.categoryId,
        categoryName: group.categoryName,
        categoryColor: category?.color || "#888",
      }))
    })
  }, [activityGroups, categories])

  const selectedActivity = allActivities.find((activity) => activity.id.toString() === value)

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" role="combobox" aria-expanded={open} className="w-full justify-between">
          {selectedActivity ? (
            <div className="flex items-center gap-2 overflow-hidden">
              <div className="h-2 w-2 rounded-full flex-shrink-0" style={{ backgroundColor: selectedActivity.categoryColor }} />
              <span className="truncate">{selectedActivity.name}</span>
              <span className="text-xs text-muted-foreground">({selectedActivity.categoryName})</span>
            </div>
          ) : (
            "활동을 선택하세요"
          )}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[--radix-popover-trigger-width] p-0">
        <Command
          filter={(value, search) => {
            const activity = allActivities.find((a) => a.id.toString() === value)
            if (!activity) return 0
            const searchText = `${activity.name.toLowerCase()} ${activity.categoryName.toLowerCase()}`
            return searchText.includes(search.toLowerCase()) ? 1 : 0
          }}
        >
          <CommandInput placeholder="활동 또는 카테고리 검색..." />
          <CommandList>
            <CommandEmpty>검색 결과가 없습니다.</CommandEmpty>
            {activityGroups.map((group) => {
              const category = categories.find((c) => c.id === group.categoryId)
              return (
                <CommandGroup key={group.categoryId} heading={group.categoryName}>
                  {group.activities.map((activity) => (
                    <CommandItem
                      key={activity.id}
                      value={activity.id.toString()}
                      onSelect={(currentValue) => {
                        const selectedId = allActivities.find(a => a.id.toString() === currentValue)?.id.toString() || ""
                        onValueChange(selectedId === value ? "" : selectedId)
                        setOpen(false)
                      }}
                    >
                      <Check
                        className={cn("mr-2 h-4 w-4", value === activity.id.toString() ? "opacity-100" : "opacity-0")}
                      />
                      <div className="flex items-center gap-2">
                         <div className="h-2 w-2 rounded-full" style={{ backgroundColor: category?.color || "#888" }} />
                         <span>{activity.name}</span>
                      </div>
                    </CommandItem>
                  ))}
                </CommandGroup>
              )
            })}
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
