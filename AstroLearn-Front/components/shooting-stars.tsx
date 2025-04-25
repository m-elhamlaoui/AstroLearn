"use client"

import { useEffect, useRef } from "react"

interface ShootingStar {
  x: number
  y: number
  length: number
  speed: number
  angle: number
  opacity: number
  active: boolean
  delay: number
}

export function ShootingStars() {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const ctx = canvas.getContext("2d")
    if (!ctx) return

    // Set canvas to full window size
    const resizeCanvas = () => {
      canvas.width = window.innerWidth
      canvas.height = window.innerHeight
    }

    window.addEventListener("resize", resizeCanvas)
    resizeCanvas()

    // Create shooting stars - REDUCED from 10 to 5
    const shootingStars: ShootingStar[] = []
    const starCount = 5 // Reduced number of shooting stars

    for (let i = 0; i < starCount; i++) {
      // Increased delay between stars (i * 5000 instead of i * 2000)
      shootingStars.push(createShootingStar(canvas.width, canvas.height, i * 5000))
    }

    // Animation loop
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      // Update and draw shooting stars
      shootingStars.forEach((star, index) => {
        if (star.delay > 0) {
          star.delay -= 16 // Approximately 16ms per frame at 60fps
          return
        }

        if (!star.active) {
          star.active = true
        }

        if (star.active) {
          // Move star
          star.x += Math.cos(star.angle) * star.speed
          star.y += Math.sin(star.angle) * star.speed

          // Draw star
          ctx.save()
          ctx.beginPath()

          // Create gradient for tail
          const gradient = ctx.createLinearGradient(
            star.x,
            star.y,
            star.x - Math.cos(star.angle) * star.length,
            star.y - Math.sin(star.angle) * star.length,
          )

          gradient.addColorStop(0, `rgba(255, 255, 255, ${star.opacity})`)
          gradient.addColorStop(1, "rgba(255, 255, 255, 0)")

          ctx.strokeStyle = gradient
          ctx.lineWidth = 2
          ctx.lineCap = "round"

          ctx.moveTo(star.x, star.y)
          ctx.lineTo(star.x - Math.cos(star.angle) * star.length, star.y - Math.sin(star.angle) * star.length)

          ctx.stroke()
          ctx.restore()

          // Reset star if it goes off screen
          if (star.x < -100 || star.x > canvas.width + 100 || star.y < -100 || star.y > canvas.height + 100) {
            // Increased delay for respawning (between 8-15 seconds)
            shootingStars[index] = createShootingStar(canvas.width, canvas.height, Math.random() * 7000 + 8000)
          }
        }
      })

      requestAnimationFrame(animate)
    }

    animate()

    return () => {
      window.removeEventListener("resize", resizeCanvas)
    }
  }, [])

  // Function to create a new shooting star
  const createShootingStar = (width: number, height: number, delay: number): ShootingStar => {
    // Random angle between -30 and -60 degrees (in radians)
    const angle = (Math.random() * 30 + 30) * (Math.PI / 180)

    return {
      x: Math.random() * width * 1.5, // Start from random position
      y: Math.random() * height * 0.3, // Start from top third of screen
      length: Math.random() * 80 + 40, // Length between 40 and 120
      speed: Math.random() * 10 + 5, // Speed between 5 and 15
      angle: angle, // Angle in radians
      opacity: Math.random() * 0.6 + 0.4, // Opacity between 0.4 and 1
      active: false,
      delay: delay, // Delay before star becomes active
    }
  }

  return <canvas ref={canvasRef} className="absolute inset-0 z-10" />
}
