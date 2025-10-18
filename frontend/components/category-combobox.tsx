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
import type { Category } from "@/lib/api"

interface CategoryComboboxProps {
  categories: Category[]
  value: string
  onValueChange: (value: string) => void
  placeholder?: string
}

export function CategoryCombobox({ categories, value, onValueChange, placeholder }: CategoryComboboxProps) {
  const [open, setOpen] = React.useState(false)

  const selectedCategory = categories.find((category) => category.id.toString() === value)

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        <Button variant="outline" role="combobox" aria-expanded={open} className="w-full justify-between">
          {selectedCategory ? (
            <div className="flex items-center gap-2 overflow-hidden">
              <div className="h-2 w-2 rounded-full flex-shrink-0" style={{ backgroundColor: selectedCategory.color }} />
              <span className="truncate">{selectedCategory.name}</span>
            </div>
          ) : (
            placeholder || "카테고리를 선택하세요"
          )}
          <ChevronsUpDown className="ml-2 h-4 w-4 shrink-0 opacity-50" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-[--radix-popover-trigger-width] p-0">
        <Command
          filter={(value, search) => {
            const category = categories.find((c) => c.id.toString() === value)
            if (!category) return 0
            return category.name.toLowerCase().includes(search.toLowerCase()) ? 1 : 0
          }}
        >
          <CommandInput placeholder="카테고리 검색..." />
          <CommandList>
            <CommandEmpty>검색 결과가 없습니다.</CommandEmpty>
            <CommandGroup>
              {categories.map((category) => (
                <CommandItem
                  key={category.id}
                  value={category.id.toString()}
                  onSelect={(currentValue) => {
                    const selectedId = categories.find(c => c.id.toString() === currentValue)?.id.toString() || ""
                    onValueChange(selectedId === value ? "" : selectedId)
                    setOpen(false)
                  }}
                >
                  <Check className={cn("mr-2 h-4 w-4", value === category.id.toString() ? "opacity-100" : "opacity-0")} />
                  <div className="flex items-center gap-2">
                    <div className="h-2 w-2 rounded-full" style={{ backgroundColor: category.color }} />
                    <span>{category.name}</span>
                  </div>
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </PopoverContent>
    </Popover>
  )
}
