"use client"

import { useEffect, useRef } from "react"

interface HexagonChartProps {
  data: number[] // 6 values for 6 axes
  labels: string[]
  color?: string
}

export function HexagonChart({ data, labels, color = "#3b82f6" }: HexagonChartProps) {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext("2d")
    if (!ctx) return

    const centerX = canvas.width / 2
    const centerY = canvas.height / 2
    const radius = Math.min(centerX, centerY) - 40

    // Clear canvas
    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // Draw background hexagon grid
    ctx.strokeStyle = "#e5e7eb"
    ctx.lineWidth = 1
    for (let i = 1; i <= 5; i++) {
      drawHexagon(ctx, centerX, centerY, (radius / 5) * i, "#e5e7eb")
    }

    // Draw axes
    ctx.strokeStyle = "#d1d5db"
    ctx.lineWidth = 1
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2
      const x = centerX + radius * Math.cos(angle)
      const y = centerY + radius * Math.sin(angle)
      ctx.beginPath()
      ctx.moveTo(centerX, centerY)
      ctx.lineTo(x, y)
      ctx.stroke()
    }

    // Draw data polygon
    ctx.fillStyle = `${color}40`
    ctx.strokeStyle = color
    ctx.lineWidth = 2
    ctx.beginPath()
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2
      const value = data[i] / 100
      const x = centerX + radius * value * Math.cos(angle)
      const y = centerY + radius * value * Math.sin(angle)
      if (i === 0) {
        ctx.moveTo(x, y)
      } else {
        ctx.lineTo(x, y)
      }
    }
    ctx.closePath()
    ctx.fill()
    ctx.stroke()

    // Draw labels
    ctx.fillStyle = "#374151"
    ctx.font = "12px sans-serif"
    ctx.textAlign = "center"
    ctx.textBaseline = "middle"
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2
      const x = centerX + (radius + 25) * Math.cos(angle)
      const y = centerY + (radius + 25) * Math.sin(angle)
      ctx.fillText(labels[i], x, y)
    }
  }, [data, labels, color])

  function drawHexagon(ctx: CanvasRenderingContext2D, x: number, y: number, radius: number, color: string) {
    ctx.strokeStyle = color
    ctx.beginPath()
    for (let i = 0; i < 6; i++) {
      const angle = (Math.PI / 3) * i - Math.PI / 2
      const px = x + radius * Math.cos(angle)
      const py = y + radius * Math.sin(angle)
      if (i === 0) {
        ctx.moveTo(px, py)
      } else {
        ctx.lineTo(px, py)
      }
    }
    ctx.closePath()
    ctx.stroke()
  }

  return <canvas ref={canvasRef} width={400} height={400} className="max-w-full" />
}
