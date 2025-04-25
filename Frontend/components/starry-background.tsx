"use client"

import { useEffect, useRef } from "react"

interface Star {
  x: number
  y: number
  size: number
  opacity: number
  baseOpacity: number
  twinkleSpeed: number
  twinkleDirection: number
}

export function StarryBackground() {
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const starsRef = useRef<Star[]>([])
  const mouseRef = useRef({ x: 0, y: 0 })
  const animationRef = useRef<number>()

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext("2d")
    if (!ctx) return

    // Set canvas to full window size
    const resizeCanvas = () => {
      if (!canvas) return

      // Set to window inner dimensions for full screen coverage
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight

      console.log("Canvas resized to:", canvas.width, canvas.height)

      // Recreate stars on resize to ensure proper distribution
      createStars(canvas.width, canvas.height)
    }

    // Create stars function
    const createStars = (width: number, height: number) => {
      const stars: Star[] = []
      const starCount = Math.floor((width * height) / 1000) // Adjust density

      console.log("Creating", starCount, "stars")

      for (let i = 0; i < starCount; i++) {
        const baseOpacity = Math.random() * 0.8 + 0.2 // Opacity between 0.2 and 1
        stars.push({
          x: Math.random() * width,
          y: Math.random() * height,
          size: Math.random() * 1.5 + 0.5, // Size between 0.5 and 2
          opacity: baseOpacity,
          baseOpacity: baseOpacity, // Store the base opacity for reference
          twinkleSpeed: Math.random() * 0.01 + 0.005, // Speed of twinkling
          twinkleDirection: Math.random() > 0.5 ? 1 : -1, // Direction of twinkling
        })
      }

      starsRef.current = stars
    }

    // Track mouse position using ref instead of state
    const handleMouseMove = (e: MouseEvent) => {
      mouseRef.current = { x: e.clientX, y: e.clientY }
    }

    // Animation loop
    const animate = () => {
      if (!ctx || !canvas) return

      ctx.clearRect(0, 0, canvas.width, canvas.height)

      // Draw stars
      starsRef.current.forEach((star) => {
        // Calculate distance from mouse to star using mouseRef
        const dx = mouseRef.current.x - star.x
        const dy = mouseRef.current.y - star.y
        const distance = Math.sqrt(dx * dx + dy * dy)

        // More gradual brightness increase when mouse is close (within 200px)
        // Using an inverse square falloff for more natural light effect
        const mouseInfluence = Math.max(0, 1 - (distance / 50) ** 2)
        const mouseBoost = mouseInfluence * 0.5 // Max 0.5 boost from mouse

        // Combine base opacity, twinkle effect, and mouse influence
        const finalOpacity = Math.min(1, star.opacity + mouseBoost)
        const finalSize = star.size * (1 + mouseInfluence) // More subtle size increase

        ctx.beginPath()

        // Create gradient for glow effect
        const gradient = ctx.createRadialGradient(star.x, star.y, 0, star.x, star.y, finalSize * 2)

        gradient.addColorStop(0, `rgba(255, 255, 255, ${finalOpacity})`)
        gradient.addColorStop(1, "rgba(255, 255, 255, 0)")

        ctx.fillStyle = gradient
        ctx.arc(star.x, star.y, finalSize, 0, Math.PI * 2)
        ctx.fill()

        // Twinkle effect
        star.opacity += star.twinkleSpeed * star.twinkleDirection

        // Change direction if opacity reaches bounds
        if (star.opacity >= star.baseOpacity + 0.2 || star.opacity <= star.baseOpacity - 0.2) {
          star.twinkleDirection *= -1
        }
      })

      animationRef.current = requestAnimationFrame(animate)
    }

    // Initial setup
    resizeCanvas()

    // Add event listeners
    window.addEventListener("resize", resizeCanvas)
    window.addEventListener("mousemove", handleMouseMove)

    // Start animation loop
    animate()

    // Cleanup
    return () => {
      window.removeEventListener("resize", resizeCanvas)
      window.removeEventListener("mousemove", handleMouseMove)
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current)
      }
    }
  }, []) // Only run on mount

  return <canvas ref={canvasRef} className="absolute inset-0 w-full h-full" style={{ width: "100%", height: "100%" }} />
}
