"use client"

import { useTheme } from "next-themes"
import { useEffect, useState } from "react"
import Image from "next/image"

export function ThemeToggle() {
  const { theme, setTheme } = useTheme()
  const [mounted, setMounted] = useState(false)

  useEffect(() => {
    setMounted(true)
  }, [])

  if (!mounted) {
    return <div className="h-10 w-10" /> // Placeholder for SSR
  }

  const toggleTheme = () => {
    setTheme(theme === "dark" ? "light" : "dark")
  }

  return (
    <div onClick={toggleTheme} className="cursor-pointer">
      <div className="relative h-10 w-10">
        {theme === "dark" ? (
          <Image src="/mode_light.png" alt="Light Mode" fill className="object-contain" />
        ) : (
          <Image src="/mode_dark.png" alt="Dark Mode" fill className="object-contain" />
        )}
      </div>
    </div>
  )
}
