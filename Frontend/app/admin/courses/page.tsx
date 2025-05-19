"use client"

import { useEffect, useState } from "react"
import Link from "next/link"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { PlusCircle, Edit, Trash2, ArrowRight } from "lucide-react"

interface Course {
  id: number
  title: string
  summary: string
  thumbnail: string
  modulesCount: number
  lessonsCount: number
  status: "DRAFT" | "PUBLISHED"
  createdAt: string
}

export default function AdminCoursesPage() {
  const router = useRouter()
  const [courses, setCourses] = useState<Course[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState("")

  // Fetch courses
  useEffect(() => {
    const fetchCourses = async () => {
      try {
        setLoading(true)
        const response = await axiosInstance.get("/courses")
        setCourses(response.data)
      } catch (err) {
        console.error("Error fetching courses:", err)
        setError("Failed to load courses")
      } finally {
        setLoading(false)
      }
    }

    fetchCourses()
  }, [])

  // Handle course deletion
  const handleDeleteCourse = async (courseId: number) => {
    if (!confirm("Are you sure you want to delete this course? This action cannot be undone.")) {
      return
    }

    try {
      await axiosInstance.delete(`/courses/${courseId}`)
      setCourses(prevCourses => 
        prevCourses.filter(course => course.id !== courseId)
      )
    } catch (err) {
      console.error("Error deleting course:", err)
      alert("Failed to delete course")
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="text-center py-10">
        <h2 className="text-2xl font-bold text-red-500">Error</h2>
        <p className="mt-2">{error}</p>
        <button 
          onClick={() => window.location.reload()}
          className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-700 rounded-md text-white"
        >
          Retry
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-8">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Courses Management</h1>
          <p className="text-gray-400 mt-2">Create and manage courses, modules, and content</p>
        </div>
        <Button 
          onClick={() => router.push("/admin/courses/new")}
          className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700"
        >
          <PlusCircle size={16} />
          <span>New Course</span>
        </Button>
      </div>

      {courses.length === 0 ? (
        <Card className="bg-gray-800 border-gray-700">
          <CardContent className="p-6 flex flex-col items-center justify-center min-h-[200px]">
            <p className="text-lg text-gray-300 mb-4">No courses have been created yet</p>
            <Button 
              onClick={() => router.push("/admin/courses/new")}
              className="flex items-center gap-2 bg-indigo-600 hover:bg-indigo-700"
            >
              <PlusCircle size={16} />
              <span>Create Your First Course</span>
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {courses.map((course) => (
            <Card key={course.id} className="bg-gray-800 border-gray-700 overflow-hidden flex flex-col">
              <div 
                className="h-48 bg-gray-700 relative" 
                style={{
                  backgroundImage: course.thumbnail ? `url(${course.thumbnail})` : 'none',
                  backgroundSize: 'cover',
                  backgroundPosition: 'center'
                }}
              >
                <div className="absolute top-2 right-2">
                  <span className={`px-2 py-1 rounded text-xs font-semibold ${
                    course.status === 'PUBLISHED' ? 'bg-green-600' : 'bg-amber-600'
                  }`}>
                    {course.status}
                  </span>
                </div>
              </div>
              
              <CardHeader>
                <CardTitle className="text-xl text-white">{course.title}</CardTitle>
                <CardDescription className="text-gray-400">
                  {course.modulesCount} modules • {course.lessonsCount} lessons
                </CardDescription>
              </CardHeader>
              
              <CardContent className="flex-grow">
                <p className="text-gray-300 text-sm">
                  {course.summary?.length > 120 
                    ? `${course.summary.substring(0, 120)}...` 
                    : course.summary || "No description provided"}
                </p>
              </CardContent>
              
              <CardFooter className="flex justify-between border-t border-gray-700 pt-4">
                <div className="flex space-x-2">
                  <Button 
                    size="sm" 
                    variant="outline"
                    className="flex items-center gap-1"
                    onClick={() => router.push(`/admin/courses/${course.id}/edit`)}
                  >
                    <Edit size={14} />
                    <span>Edit</span>
                  </Button>
                  <Button 
                    size="sm" 
                    variant="destructive"
                    className="flex items-center gap-1"
                    onClick={() => handleDeleteCourse(course.id)}
                  >
                    <Trash2 size={14} />
                    <span>Delete</span>
                  </Button>
                </div>
                <Button 
                  size="sm"
                  className="flex items-center gap-1 bg-indigo-600 hover:bg-indigo-700"
                  onClick={() => router.push(`/admin/courses/${course.id}`)}
                >
                  <span>Manage</span>
                  <ArrowRight size={14} />
                </Button>
              </CardFooter>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}
