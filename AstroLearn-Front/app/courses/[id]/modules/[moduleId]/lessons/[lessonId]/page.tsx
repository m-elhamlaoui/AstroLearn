"use client"

import { useState, useEffect } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft, ArrowRight, CheckCircle } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"

// Sample lesson data - would be fetched from backend in production
const getLessonData = (courseId: string, moduleId: string, lessonId: string) => {
  return {
    id: Number(lessonId),
    title: "Understanding the Celestial Sphere",
    videoUrl: "https://www.youtube.com/embed/dQw4w9WgXcQ", // Example video URL
    content: `
      <h2>The Celestial Sphere Concept</h2>
      
      <p>The celestial sphere is an imaginary sphere of arbitrarily large radius, concentric with Earth. All objects in the sky can be conceived as being projected upon the inner surface of the celestial sphere, which may be centered on Earth or the observer. If centered on the observer, half of the sphere would resemble a hemispherical screen over the observing location.</p>
      
      <p>The celestial sphere is a practical tool for spherical astronomy, allowing astronomers to plot positions of objects in the sky without considering their precise distances.</p>
      
      <h3>Key Elements of the Celestial Sphere</h3>
      
      <ul>
        <li><strong>Celestial Poles:</strong> The two points where Earth's rotational axis intersects the celestial sphere.</li>
        <li><strong>Celestial Equator:</strong> The great circle on the celestial sphere that is in the same plane as Earth's equator.</li>
        <li><strong>Ecliptic:</strong> The apparent path of the Sun across the celestial sphere over the course of a year.</li>
        <li><strong>Celestial Meridian:</strong> The great circle passing through the celestial poles and the observer's zenith.</li>
        <li><strong>Zenith and Nadir:</strong> The points directly above and below the observer, respectively.</li>
      </ul>
      
      <h3>Coordinate Systems</h3>
      
      <p>Several coordinate systems are used with the celestial sphere:</p>
      
      <ul>
        <li><strong>Horizontal (Altitude-Azimuth):</strong> Coordinates relative to the observer's horizon and zenith.</li>
        <li><strong>Equatorial:</strong> Coordinates based on the celestial equator and celestial poles.</li>
        <li><strong>Ecliptic:</strong> Coordinates based on the ecliptic and the ecliptic poles.</li>
        <li><strong>Galactic:</strong> Coordinates based on the plane of the Milky Way galaxy.</li>
      </ul>
      
      <p>Understanding these coordinate systems is essential for locating and tracking celestial objects in the night sky.</p>
    `,
    courseId: Number(courseId),
    moduleId: Number(moduleId),
    completed: false,
    nextLesson: {
      moduleId: 1,
      lessonId: 102,
    },
    prevLesson: null, // This is the first lesson
  }
}

export default function LessonPage({
  params,
}: {
  params: { id: string; moduleId: string; lessonId: string }
}) {
  const router = useRouter()
  const lesson = getLessonData(params.id, params.moduleId, params.lessonId)
  const [isCompleted, setIsCompleted] = useState(lesson.completed)

  // Mark lesson as completed when the page is viewed
  useEffect(() => {
    // In a real app, this would call an API to mark the lesson as completed
    if (!isCompleted) {
      // Simulate API call delay
      const timer = setTimeout(() => {
        setIsCompleted(true)
      }, 2000)

      return () => clearTimeout(timer)
    }
  }, [isCompleted])

  // Handle navigation to next lesson
  const handleNextLesson = () => {
    if (lesson.nextLesson) {
      router.push(`/courses/${params.id}/modules/${lesson.nextLesson.moduleId}/lessons/${lesson.nextLesson.lessonId}`)
    } else {
      // If there's no next lesson, go back to course page
      router.push(`/courses/${params.id}`)
    }
  }

  // Handle navigation to previous lesson
  const handlePrevLesson = () => {
    if (lesson.prevLesson) {
      router.push(`/courses/${params.id}/modules/${lesson.prevLesson.moduleId}/lessons/${lesson.prevLesson.lessonId}`)
    } else {
      // If there's no previous lesson, go back to course page
      router.push(`/courses/${params.id}`)
    }
  }

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
        <div className="container mx-auto max-w-4xl">
          {/* Back Button */}
          <Link
            href={`/courses/${params.id}`}
            className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Course
          </Link>

          {/* Lesson Title */}
          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-bold">{lesson.title}</h1>
            {isCompleted && (
              <div className="flex items-center gap-2 text-green-500">
                <CheckCircle className="h-5 w-5" />
                <span>Completed</span>
              </div>
            )}
          </div>

          {/* Video Section */}
          <div className="mb-8">
            <div className="relative pb-[56.25%] h-0 overflow-hidden rounded-xl">
              <iframe
                src={lesson.videoUrl}
                className="absolute top-0 left-0 w-full h-full"
                frameBorder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
              ></iframe>
            </div>
          </div>

          {/* Lesson Content */}
          <div className="mb-10">
            <div className="prose prose-invert max-w-none" dangerouslySetInnerHTML={{ __html: lesson.content }} />
          </div>

          {/* Navigation Buttons */}
          <div className="flex justify-between mt-10">
            <Button
              onClick={handlePrevLesson}
              variant="outline"
              className="border-gray-700 text-gray-300 hover:bg-gray-800"
              disabled={!lesson.prevLesson}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Previous Lesson
            </Button>

            <Button onClick={handleNextLesson} className="bg-white text-black hover:bg-gray-200">
              {lesson.nextLesson ? (
                <>
                  Next Lesson
                  <ArrowRight className="h-4 w-4 ml-2" />
                </>
              ) : (
                "Complete Course"
              )}
            </Button>
          </div>
        </div>
      </main>
    </div>
  )
}
