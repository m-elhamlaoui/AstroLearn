"use client"

import { useState, useEffect, use } from "react" // Added use
import axiosInstance from "@/lib/axiosInstance" 
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Star, Users, Clock, ArrowLeft, CheckCircle, Play, UserCircle2 } from "lucide-react" // Added UserCircle2
import Link from "next/link"
import Image from "next/image"

// Define interfaces for backend DTOs and page data structure
interface CourseDTO {
  id: number;
  title: string;
  imageUrl: string;
  description: string;
  difficulty: string; // e.g., "BEGINNER", "INTERMEDIATE", "ADVANCED"
  totalLessons: number;
  moduleIds: number[];
}

interface ModuleDTO {
  id: number;
  title: string;
  courseId: number;
  lessonCount: number;
  lessonIds: number[];
  // description is not in ModuleDTO, will use title or placeholder
}

interface LessonDTO {
  id: number;
  title: string;
  content: string; // Assuming content can be used for a brief description if needed
  videoUrl: string | null;
  moduleId: number;
  quizId: number | null;
  // duration and completed are not in LessonDTO
}

interface CourseProgressDTO {
  courseId: number;
  courseTitle: string;
  userId: number;
  username: string;
  totalLessons: number;
  completedLessons: number;
  completionPercentage: number;
  completed: boolean;
  currentLessonId: number | null;
  completedLessonIds: number[];
}

interface PageLesson {
  id: number;
  title: string;
  duration: string; // Placeholder, as not in DTO
  completed: boolean;
}

interface PageModule {
  id: number;
  title: string;
  description: string; // Placeholder or derived
  lessons: PageLesson[];
}

interface PageCourse {
  id: number;
  title: string;
  description: string;
  longDescription: string; // Will use DTO's description, rendered as text
  image: string;
  instructor: string; // Placeholder
  instructorBio: string; // Placeholder
  instructorImage: string; // Placeholder
  level: string;
  duration: string; // Placeholder
  category: string; // Placeholder
  rating: number; // Placeholder
  studentsCount: number; // Placeholder
  modules: PageModule[];
}

export default function CoursePage({ params: paramsPromise }: { params: Promise<{ id: string }> }) { // Modified params prop
  const params = use(paramsPromise); // Resolve the params promise

  const [course, setCourse] = useState<PageCourse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [expandedModules, setExpandedModules] = useState<string[]>([])

  // State to store user ID from localStorage
  const [userId, setUserId] = useState<number | null>(null)
  // State to store overall course progress percentage
  const [courseProgress, setCourseProgress] = useState<number>(0)
  // State to store API-tracked completed lessons count
  const [apiCompletedLessons, setApiCompletedLessons] = useState<number>(0)
  // State to track if we need to refresh course data
  const [refreshTrigger, setRefreshTrigger] = useState<number>(0)

  useEffect(() => {
    // Get user ID from localStorage
    if (typeof window !== 'undefined') {
      const storedUserId = localStorage.getItem('userId')
      if (storedUserId) {
        setUserId(parseInt(storedUserId, 10))
      }
      
      // Set up a global function to refresh course progress
      // Define the type for the window object with our custom property
      (window as any).updateCourseProgress = () => {
        console.log('Course progress update triggered from lesson page')
        setRefreshTrigger(prev => prev + 1)
      }
      
      // Clean up the global function when component unmounts
      return () => {
        (window as any).updateCourseProgress = undefined
      }
    }
  }, [])

  useEffect(() => {
    // Skip fetching if no userId is available yet
    if (!userId) return;
    
    const fetchCourseData = async () => {
      // Check for token before fetching
      if (typeof window !== 'undefined' && !localStorage.getItem('authToken')) {
        // No token, useAuthRedirect will handle redirection.
        setIsLoading(false); // Stop loading as we won't fetch
        return; 
      }

      setIsLoading(true)
      setError(null)

      try {
        // 1. Fetch Course Details
        const courseResponse = await axiosInstance.get<CourseDTO>(`/courses/${params.id}`)
        const courseData = courseResponse.data
        
        // 2. Fetch Modules for the Course
        const modulesResponse = await axiosInstance.get<ModuleDTO[]>(`/modules/courses/${courseData.id}`)
        const moduleDataArray = modulesResponse.data

        // 3. Fetch Course Progress
        let completedLessonIdsSet = new Set<number>()
        let overallProgressPercentage = 0
        let totalCompletedLessons = 0
        
        // Get user ID from localStorage
        const userId = localStorage.getItem('userId')
        if (userId) {
          try {
              const progressResponse = await axiosInstance.get<CourseProgressDTO>(`/api/course-progress/${userId}/${courseData.id}/progress`)
              if (progressResponse.data) {
                  const progressData = progressResponse.data;
                  overallProgressPercentage = progressData.completionPercentage;
                  setCourseProgress(overallProgressPercentage);
                  
                  // Update completed lessons tracking based on API data
                  if (progressData.completedLessonIds && progressData.completedLessonIds.length > 0) {
                    completedLessonIdsSet = new Set(progressData.completedLessonIds);
                    totalCompletedLessons = progressData.completedLessonIds.length;
                    setApiCompletedLessons(totalCompletedLessons);
                  }
                  
                  console.log('Course progress fetched successfully:', progressData);
              }
          } catch (err) {
              console.error('Error fetching course progress:', err)
              // Don't set progress to 0 if there's an error
              // Continue without progress data if it fails
          }
        }

        // 4. Fetch Lessons for each Module and map
        const pageModules: PageModule[] = await Promise.all(
          moduleDataArray.map(async (moduleDto) => {
            const lessonsResponse = await axiosInstance.get<LessonDTO[]>(`/lessons/modules/${moduleDto.id}`)
            const lessonDataArray = lessonsResponse.data

            const pageLessons: PageLesson[] = lessonDataArray.map((lessonDto) => ({
              id: lessonDto.id,
              title: lessonDto.title,
              duration: "N/A", // Placeholder for lesson duration
              completed: completedLessonIdsSet.has(lessonDto.id),
            }))

            return {
              id: moduleDto.id,
              title: moduleDto.title,
              description: moduleDto.title, // Using title as placeholder for module description
              lessons: pageLessons,
            }
          }),
        )

        // 5. Map to PageCourse structure
        const difficultyMap: { [key: string]: string } = {
          BEGINNER: "Beginner",
          INTERMEDIATE: "Intermediate",
          ADVANCED: "Advanced",
        }

        setCourse({
          id: courseData.id,
          title: courseData.title,
          description: courseData.description,
          longDescription: `<p>${courseData.description.replace(/\n/g, "</p><p>")}</p>`, // Basic formatting for long description
          image: courseData.imageUrl || "/placeholder.svg?height=500&width=1000", // Use S3/backend URL or fallback
          instructor: "AstroLearn Faculty", // Placeholder
          instructorBio: "Dedicated educators passionate about space and technology.", // Placeholder
          instructorImage: "/placeholder.svg?height=100&width=100", // Placeholder
          level: difficultyMap[courseData.difficulty.toUpperCase()] || "N/A",
          duration: "N/A", // Placeholder for course duration
          category: "Space Science", // Placeholder
          rating: 0, // Placeholder
          studentsCount: 0, // Placeholder
          modules: pageModules,
        })
      } catch (err) {
        console.error("Failed to fetch course details:", err)
        setError("Failed to load course details. Please try again later.")
      } finally {
        setIsLoading(false)
      }
    }

    if (params.id) {
      fetchCourseData()
    }
  }, [params.id, userId, refreshTrigger])

  if (isLoading) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Loading course details...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl text-red-500">{error}</p>
      </div>
    )
  }

  if (!course) {
    return (
      <div className="flex min-h-screen bg-black text-white justify-center items-center">
        <p className="text-xl">Course not found.</p>
      </div>
    )
  }

  // Calculate progress (moved here to ensure 'course' is not null)
  const totalLessons = course.modules.reduce((total, module) => total + module.lessons.length, 0)
  const completedLessons = course.modules.reduce(
    (total, module) => total + module.lessons.filter((lesson) => lesson.completed).length,
    0,
  )
  const progressPercentage = totalLessons > 0 ? Math.round((completedLessons / totalLessons) * 100) : 0

  // Find first incomplete lesson for "Start Course" button
  const firstIncompleteLesson = course.modules
    .flatMap((module) => module.lessons.map(lesson => ({ ...lesson, moduleId: module.id })))
    .find((lesson) => !lesson.completed)

  // If all lessons are completed, or no lessons, use the first lesson of the first module if available
  const startLessonLink = firstIncompleteLesson
    ? `/courses/${course.id}/modules/${firstIncompleteLesson.moduleId}/lessons/${firstIncompleteLesson.id}`
    : (course.modules.length > 0 && course.modules[0].lessons.length > 0
        ? `/courses/${course.id}/modules/${course.modules[0].id}/lessons/${course.modules[0].lessons[0].id}`
        : `/courses/${course.id}`); // Fallback if no lessons

  return (
    <div className="flex min-h-screen bg-black text-white">
      {/* Minimal Navigation */}
      <MinimalNavigation />

      {/* Main Content */}
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
        <div className="container mx-auto max-w-5xl">
          {/* Back Button */}
          <Link href="/courses" className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6">
            <ArrowLeft className="h-4 w-4" />
            Back to Courses
          </Link>

          {/* Course Header */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">
            {/* Course Image */}
            <div className="md:col-span-1">
              <div className="relative h-60 rounded-xl overflow-hidden">
                <Image src={course.image} alt={course.title} fill className="object-cover" />
              </div>
            </div>

            {/* Course Info */}
            <div className="md:col-span-2">
              <h1 className="text-3xl font-bold mb-4">{course.title}</h1>

              <p className="text-gray-300 mb-4">{course.description}</p>

              <div className="flex flex-wrap gap-4 mb-4">
                <Badge className="bg-gray-800 text-white px-3 py-1">{course.level}</Badge>
                <Badge className="bg-gray-800 text-white px-3 py-1">{course.duration}</Badge>
                <Badge className="bg-gray-800 text-white px-3 py-1">{course.category}</Badge>
              </div>

              <div className="flex flex-wrap gap-6 mb-6">
                {course.rating > 0 && (
                  <div className="flex items-center gap-1">
                    <Star className="h-5 w-5 text-yellow-500 fill-yellow-500" />
                    <span className="font-medium">{course.rating}</span>
                  </div>
                )}

                {course.studentsCount > 0 && (
                  <div className="flex items-center gap-1 text-gray-300">
                    <Users className="h-5 w-5" />
                    <span>{course.studentsCount.toLocaleString()} students</span>
                  </div>
                )}

                <div className="flex items-center gap-1 text-gray-300">
                  <Clock className="h-5 w-5" />
                  <span>{course.duration}</span>
                </div>
              </div>

              {/* Course Progress */}
              <div className="mb-6">
                <div className="flex justify-between text-sm mb-1">
                  <span>Your Progress</span>
                  <span>{Math.max(courseProgress, progressPercentage)}% Complete</span>
                </div>
                <div className="w-full bg-gray-800 rounded-full h-2.5">
                  <div 
                    className="bg-green-600 h-2.5 rounded-full" 
                    style={{ width: `${Math.max(courseProgress, progressPercentage)}%` }}
                  ></div>
                </div>
              </div>

              {/* Start Course Button */}
              <Link href={startLessonLink}>
                <Button className="bg-white text-black hover:bg-gray-200 px-6 py-2 rounded-lg">
                  <Play className="h-4 w-4 mr-2" />
                  {apiCompletedLessons > 0 || completedLessons > 0 ? "Continue Course" : "Start Course"}
                </Button>
              </Link>
            </div>
          </div>
          {/* Instructor Info */}
          <div className="bg-gray-900 rounded-xl p-6 mb-10">
            <div className="flex items-start gap-4">
              <div className="relative h-16 w-16 rounded-full overflow-hidden bg-gray-800 flex items-center justify-center">
                {course.instructorImage && course.instructorImage !== "/placeholder.svg?height=100&width=100" ? (
                  <Image
                    src={course.instructorImage}
                    alt={course.instructor}
                    fill
                    className="object-cover"
                  />
                ) : (
                  <UserCircle2 className="h-10 w-10 text-gray-500" /> // Default icon
                )}
              </div>
              <div>
                <h3 className="text-xl font-bold mb-1">Instructor: {course.instructor}</h3>
                <p className="text-gray-400">{course.instructorBio}</p>
              </div>
            </div>
          </div>

          {/* Course Description */}
          <div className="mb-10">
            <h2 className="text-2xl font-bold mb-4">About This Course</h2>
            <div
              className="prose prose-invert max-w-none"
              dangerouslySetInnerHTML={{ __html: course.longDescription }}
            />
          </div>

          {/* Course Content */}
          <div>
            <h2 className="text-2xl font-bold mb-4">Course Content</h2>
            <div className="bg-gray-900 rounded-xl overflow-hidden">
              <Accordion type="multiple" value={expandedModules} onValueChange={setExpandedModules} className="w-full">
                {course.modules.map((module) => {
                  const completedModuleLessons = module.lessons.filter((lesson) => lesson.completed).length
                  const totalModuleLessons = module.lessons.length
                  const moduleProgress = Math.round((completedModuleLessons / totalModuleLessons) * 100)

                  return (
                    <AccordionItem
                      key={module.id}
                      value={`module-${module.id}`}
                      className="border-b border-gray-800 last:border-0"
                    >
                      <AccordionTrigger className="px-6 py-4 hover:bg-gray-800 transition-colors">
                        <div className="flex-1 text-left">
                          <div className="flex justify-between items-center">
                            <h3 className="text-lg font-medium">
                              Module {module.id}: {module.title}
                            </h3>
                            <div className="text-sm text-gray-400">
                              {completedModuleLessons}/{totalModuleLessons} lessons
                            </div>
                          </div>
                          <p className="text-sm text-gray-400 mt-1">{module.description}</p>
                          <div className="w-full bg-gray-800 rounded-full h-1.5 mt-2">
                            <div
                              className="bg-green-600 h-1.5 rounded-full"
                              style={{ width: `${moduleProgress}%` }}
                            ></div>
                          </div>
                        </div>
                      </AccordionTrigger>
                      <AccordionContent className="px-6 py-2">
                        <ul className="space-y-2">
                          {module.lessons.map((lesson) => (
                            <li key={lesson.id}>
                              <Link
                                href={`/courses/${course.id}/modules/${module.id}/lessons/${lesson.id}`}
                                className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-800 transition-colors"
                              >
                                <div className="flex items-center gap-3">
                                  {lesson.completed ? (
                                    <CheckCircle className="h-5 w-5 text-green-500" data-testid={`completed-lesson-${lesson.id}`} />
                                  ) : (
                                    <Play className="h-5 w-5 text-gray-400" data-testid={`uncompleted-lesson-${lesson.id}`} />
                                  )}
                                  <span className={lesson.completed ? "text-gray-300" : "text-white"}>
                                    {lesson.title}
                                  </span>
                                </div>
                                <span className="text-sm text-gray-400">{lesson.duration}</span>
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </AccordionContent>
                    </AccordionItem>
                  )
                })}
              </Accordion>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}
