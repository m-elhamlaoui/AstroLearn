"use client"

import { useState, useEffect, use } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { ArrowLeft, ArrowRight, CheckCircle } from "lucide-react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"

// DTO interfaces (subset of what's in dtos.txt, focused on needs)
interface LessonDTO {
  id: number
  title: string
  content: string
  videoUrl: string | null
  moduleId: number
  quizId: number | null
}

interface ModuleWithLessonsDTO {
  id: number
  title: string
  lessons: { id: number; title: string }[] // Simplified lesson for navigation structure
}

interface CourseProgressDTO {
  completedLessonIds: number[]
}

interface LessonPageParams {
  id: string // courseId
  moduleId: string
  lessonId: string
}

interface NavLesson {
  courseId: string
  moduleId: string
  lessonId: string
}

export default function LessonPage({ params: paramsPromise }: { params: Promise<LessonPageParams> }) {
  const params = use(paramsPromise)
  const router = useRouter()

  const [lessonDetails, setLessonDetails] = useState<LessonDTO | null>(null)
  const [isCompleted, setIsCompleted] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [nextLesson, setNextLesson] = useState<NavLesson | null>(null)
  const [prevLesson, setPrevLesson] = useState<NavLesson | null>(null)

  // TODO: Replace with actual logged-in user ID
  const userIdForProgress = 1

  useEffect(() => {
    if (!params.id || !params.moduleId || !params.lessonId) {
      setError("Missing course, module, or lesson ID.")
      setIsLoading(false)
      return
    }

    const fetchLessonAllData = async () => {
      setIsLoading(true)
      setError(null)

      try {
        // 1. Fetch current lesson details
        const lessonResponse = await axiosInstance.get<LessonDTO>(`/lessons/${params.lessonId}`)
        setLessonDetails(lessonResponse.data)

        // 2. Fetch course progress to check completion status
        try {
          const progressResponse = await axiosInstance.get<CourseProgressDTO>(
            `/course-progress/${userIdForProgress}/${params.id}`,
          )
          const completedIds = new Set(progressResponse.data.completedLessonIds || [])
          const currentLessonIsCompleted = completedIds.has(Number(params.lessonId))
          setIsCompleted(currentLessonIsCompleted)

          // 3. Mark lesson as complete if not already (and if lesson data is fetched)
          if (lessonResponse.data && !currentLessonIsCompleted) {
            try {
              await axiosInstance.post(
                `/course-progress/${userIdForProgress}/${params.id}/lessons/${params.lessonId}/complete`,
              )
              setIsCompleted(true) // Update UI optimistically or after success
            } catch (completionError) {
              console.warn("Failed to mark lesson as complete:", completionError)
              // Decide if this is a critical error or can be ignored for the user
            }
          }
        } catch (progressError) {
          console.warn("Failed to fetch or update course progress:", progressError)
          // Continue without progress data if it fails, lesson will appear incomplete
        }
        
        // 4. Fetch full course structure for navigation
        const courseModulesResponse = await axiosInstance.get<
          { id: number; title: string; lessonIds: number[] }[] // Assuming ModuleDTO from course page
        >(`/modules/courses/${params.id}`)
        
        const allLessonsInCourseFlat: NavLesson[] = []
        for (const module of courseModulesResponse.data) {
          const lessonsInModuleResponse = await axiosInstance.get<LessonDTO[]>(`/lessons/modules/${module.id}`)
          lessonsInModuleResponse.data.forEach(lesson => {
            allLessonsInCourseFlat.push({ 
              courseId: params.id, 
              moduleId: String(module.id), 
              lessonId: String(lesson.id) 
            })
          })
        }

        const currentLessonIndex = allLessonsInCourseFlat.findIndex(
          (l) => l.lessonId === params.lessonId && l.moduleId === params.moduleId,
        )

        if (currentLessonIndex !== -1) {
          if (currentLessonIndex > 0) {
            setPrevLesson(allLessonsInCourseFlat[currentLessonIndex - 1])
          } else {
            setPrevLesson(null)
          }
          if (currentLessonIndex < allLessonsInCourseFlat.length - 1) {
            setNextLesson(allLessonsInCourseFlat[currentLessonIndex + 1])
          } else {
            setNextLesson(null)
          }
        }

      } catch (err) {
        console.error("Failed to load lesson data:", err)
        setError("Failed to load lesson. Please try again later.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchLessonAllData()
  }, [params.id, params.moduleId, params.lessonId]) // Re-fetch if any param changes

  const handleNavigation = (navLesson: NavLesson | null) => {
    if (navLesson) {
      router.push(`/courses/${navLesson.courseId}/modules/${navLesson.moduleId}/lessons/${navLesson.lessonId}`)
    } else {
      // If no next/prev lesson (e.g., end of course), navigate to course page
      router.push(`/courses/${params.id}`)
    }
  }
  
  if (isLoading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Loading lesson...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center p-8">
        <div className="text-center">
          <p className="text-xl text-red-500 mb-4">{error}</p>
          <Link href={`/courses/${params.id || ''}`}>
            <Button variant="outline">Back to Course</Button>
          </Link>
        </div>
      </div>
    )
  }

  if (!lessonDetails) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Lesson not found.</p>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen bg-black text-white">
      <MinimalNavigation />
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
        <div className="container mx-auto max-w-4xl">
          <Link
            href={`/courses/${params.id}`}
            className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6"
          >
            <ArrowLeft className="h-4 w-4" />
            Back to Course
          </Link>

          <div className="flex items-center justify-between mb-6">
            <h1 className="text-2xl font-bold">{lessonDetails.title}</h1>
            {isCompleted && (
              <div className="flex items-center gap-2 text-green-500">
                <CheckCircle className="h-5 w-5" />
                <span>Completed</span>
              </div>
            )}
          </div>

          {lessonDetails.videoUrl && (
            <div className="mb-8">
              <div className="relative pb-[56.25%] h-0 overflow-hidden rounded-xl shadow-2xl">
                <iframe
                  src={lessonDetails.videoUrl}
                  className="absolute top-0 left-0 w-full h-full"
                  title={lessonDetails.title}
                  frameBorder="0"
                  allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                  allowFullScreen
                ></iframe>
              </div>
            </div>
          )}

          <div className="mb-10 prose prose-invert max-w-none bg-gray-900/50 p-6 rounded-xl" 
               dangerouslySetInnerHTML={{ __html: lessonDetails.content }} />

          <div className="flex justify-between mt-10">
            <Button
              onClick={() => handleNavigation(prevLesson)}
              variant="outline"
              className="border-gray-700 text-gray-300 hover:bg-gray-800 disabled:opacity-50"
              disabled={!prevLesson}
            >
              <ArrowLeft className="h-4 w-4 mr-2" />
              Previous Lesson
            </Button>

            <Button 
              onClick={() => handleNavigation(nextLesson)} 
              className="bg-white text-black hover:bg-gray-200 disabled:opacity-50"
              // No explicit disabled prop needed if handleNavigation handles null nextLesson to go to course page
            >
              {nextLesson ? (
                <>
                  Next Lesson
                  <ArrowRight className="h-4 w-4 ml-2" />
                </>
              ) : (
                "Back to Course" // Or "Finish Course"
              )}
            </Button>
          </div>
        </div>
      </main>
    </div>
  )
}
