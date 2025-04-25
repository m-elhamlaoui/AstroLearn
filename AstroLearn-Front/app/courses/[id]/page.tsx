"use client"

import { useState } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Star, Users, Clock, ArrowLeft, CheckCircle, Play } from "lucide-react"
import Link from "next/link"
import Image from "next/image"

// Sample course data - would be fetched from backend in production
const getCourseById = (id: string) => {
  return {
    id: Number(id),
    title: "Introduction to Astronomy",
    description:
      "Learn the fundamentals of astronomy, from celestial objects to the structure of the universe. This comprehensive course covers everything from the basics of stargazing to the complex physics of black holes.",
    longDescription: `
      <p>Astronomy is the scientific study of celestial objects (such as stars, planets, comets, and galaxies), the physics, chemistry, and evolution of such objects, and phenomena that originate outside the Earth's atmosphere (such as cosmic microwave background radiation).</p>
      
      <p>In this course, you will learn about:</p>
      
      <ul>
        <li>The history of astronomy and how our understanding of the universe has evolved</li>
        <li>The solar system and its components, including planets, moons, asteroids, and comets</li>
        <li>Stars, their life cycles, and stellar evolution</li>
        <li>Galaxies, their types, and the structure of the universe</li>
        <li>Modern astronomical tools and techniques</li>
        <li>Current research and discoveries in the field</li>
      </ul>
      
      <p>By the end of this course, you will have a solid foundation in astronomical concepts and be prepared for more advanced studies in astrophysics and space science.</p>
    `,
    image: "/placeholder.svg?height=500&width=1000",
    instructor: "Dr. Elena Rodriguez",
    instructorBio: "Astrophysicist with 15 years of experience in research and education. Former NASA scientist.",
    instructorImage: "/placeholder.svg?height=100&width=100",
    level: "Beginner",
    duration: "4 weeks",
    category: "Astronomy Basics",
    rating: 4.8,
    studentsCount: 1245,
    modules: [
      {
        id: 1,
        title: "Introduction to the Night Sky",
        description: "Learn about constellations, celestial coordinates, and basic stargazing.",
        lessons: [
          {
            id: 101,
            title: "Understanding the Celestial Sphere",
            duration: "15 min",
            completed: true,
          },
          {
            id: 102,
            title: "Major Constellations and How to Find Them",
            duration: "20 min",
            completed: true,
          },
          {
            id: 103,
            title: "Celestial Coordinates and Star Charts",
            duration: "25 min",
            completed: false,
          },
        ],
      },
      {
        id: 2,
        title: "The Solar System",
        description: "Explore our cosmic neighborhood, including planets, moons, and other objects.",
        lessons: [
          {
            id: 201,
            title: "The Sun: Our Star",
            duration: "30 min",
            completed: false,
          },
          {
            id: 202,
            title: "Inner Planets: Mercury, Venus, Earth, and Mars",
            duration: "45 min",
            completed: false,
          },
          {
            id: 203,
            title: "Outer Planets: Jupiter, Saturn, Uranus, and Neptune",
            duration: "45 min",
            completed: false,
          },
          {
            id: 204,
            title: "Dwarf Planets, Asteroids, and Comets",
            duration: "30 min",
            completed: false,
          },
        ],
      },
      {
        id: 3,
        title: "Stars and Stellar Evolution",
        description: "Learn about the life cycles of stars from birth to death.",
        lessons: [
          {
            id: 301,
            title: "Star Formation and Classification",
            duration: "35 min",
            completed: false,
          },
          {
            id: 302,
            title: "Main Sequence Stars",
            duration: "25 min",
            completed: false,
          },
          {
            id: 303,
            title: "Red Giants and Supergiants",
            duration: "20 min",
            completed: false,
          },
          {
            id: 304,
            title: "Stellar Death: White Dwarfs, Neutron Stars, and Black Holes",
            duration: "40 min",
            completed: false,
          },
        ],
      },
      {
        id: 4,
        title: "Galaxies and Cosmology",
        description: "Explore the larger structures of the universe and theories about its origin and evolution.",
        lessons: [
          {
            id: 401,
            title: "Galaxy Types and Formation",
            duration: "30 min",
            completed: false,
          },
          {
            id: 402,
            title: "The Milky Way Galaxy",
            duration: "25 min",
            completed: false,
          },
          {
            id: 403,
            title: "The Big Bang Theory",
            duration: "35 min",
            completed: false,
          },
          {
            id: 404,
            title: "Dark Matter and Dark Energy",
            duration: "40 min",
            completed: false,
          },
        ],
      },
    ],
  }
}

export default function CoursePage({ params }: { params: { id: string } }) {
  const course = getCourseById(params.id)
  const [expandedModules, setExpandedModules] = useState<string[]>([])

  // Calculate progress
  const totalLessons = course.modules.reduce((total, module) => total + module.lessons.length, 0)
  const completedLessons = course.modules.reduce(
    (total, module) => total + module.lessons.filter((lesson) => lesson.completed).length,
    0,
  )
  const progressPercentage = Math.round((completedLessons / totalLessons) * 100)

  // Find first incomplete lesson for "Start Course" button
  const firstIncompleteLesson = course.modules
    .map((module) => {
      const incompleteLesson = module.lessons.find((lesson) => !lesson.completed)
      return incompleteLesson ? { moduleId: module.id, lessonId: incompleteLesson.id } : null
    })
    .filter(Boolean)[0]

  // If all lessons are completed, use the first lesson
  const startLessonLink = firstIncompleteLesson
    ? `/courses/${course.id}/modules/${firstIncompleteLesson.moduleId}/lessons/${firstIncompleteLesson.lessonId}`
    : `/courses/${course.id}/modules/${course.modules[0].id}/lessons/${course.modules[0].lessons[0].id}`

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
                <Image src={course.image || "/placeholder.svg"} alt={course.title} fill className="object-cover" />
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
                <div className="flex items-center gap-1">
                  <Star className="h-5 w-5 text-yellow-500 fill-yellow-500" />
                  <span className="font-medium">{course.rating}</span>
                </div>

                <div className="flex items-center gap-1 text-gray-300">
                  <Users className="h-5 w-5" />
                  <span>{course.studentsCount.toLocaleString()} students</span>
                </div>

                <div className="flex items-center gap-1 text-gray-300">
                  <Clock className="h-5 w-5" />
                  <span>{course.duration}</span>
                </div>
              </div>

              {/* Progress Bar */}
              <div className="mb-6">
                <div className="flex justify-between text-sm mb-1">
                  <span>Course Progress</span>
                  <span>{progressPercentage}% Complete</span>
                </div>
                <div className="w-full bg-gray-800 rounded-full h-2.5">
                  <div className="bg-green-600 h-2.5 rounded-full" style={{ width: `${progressPercentage}%` }}></div>
                </div>
              </div>

              {/* Start Course Button */}
              <Link href={startLessonLink}>
                <Button className="bg-white text-black hover:bg-gray-200 px-6 py-2 rounded-lg">
                  <Play className="h-4 w-4 mr-2" />
                  {completedLessons > 0 ? "Continue Course" : "Start Course"}
                </Button>
              </Link>
            </div>
          </div>

          {/* Instructor Info */}
          <div className="bg-gray-900 rounded-xl p-6 mb-10">
            <div className="flex items-start gap-4">
              <div className="relative h-16 w-16 rounded-full overflow-hidden">
                <Image
                  src={course.instructorImage || "/placeholder.svg"}
                  alt={course.instructor}
                  fill
                  className="object-cover"
                />
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
                                    <CheckCircle className="h-5 w-5 text-green-500" />
                                  ) : (
                                    <Play className="h-5 w-5 text-gray-400" />
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
