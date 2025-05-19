"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { toast } from "react-hot-toast"
import { ArrowLeft } from "lucide-react"

export default function NewCoursePage() {
  const router = useRouter()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [courseData, setCourseData] = useState({
    title: "",
    summary: "",
    description: "",
    thumbnail: "",
    difficulty: "BEGINNER",
    estimatedHours: 1,
    status: "DRAFT"
  })

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setCourseData(prev => ({ ...prev, [name]: value }))
  }

  const handleSelectChange = (name: string, value: string) => {
    setCourseData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!courseData.title.trim()) {
      toast.error("Course title is required")
      return
    }
    
    try {
      setIsSubmitting(true)
      const response = await axiosInstance.post("/courses", courseData)
      toast.success("Course created successfully")
      router.push(`/admin/courses/${response.data.id}`)
    } catch (err) {
      console.error("Error creating course:", err)
      toast.error("Failed to create course")
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="space-y-8">
      <div className="flex items-center gap-4">
        <Button
          variant="ghost"
          size="icon"
          onClick={() => router.push("/admin/courses")}
          className="rounded-full text-gray-400 hover:text-white hover:bg-gray-700"
        >
          <ArrowLeft size={20} />
        </Button>
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Create New Course</h1>
          <p className="text-gray-400 mt-2">Set up the basic information for your new course</p>
        </div>
      </div>

      <form onSubmit={handleSubmit}>
        <Card className="bg-gray-800 border-gray-700">
          <CardHeader>
            <CardTitle className="text-xl text-white">Course Details</CardTitle>
          </CardHeader>
          
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <Label htmlFor="title">Course Title*</Label>
              <Input
                id="title"
                name="title"
                placeholder="Enter course title"
                value={courseData.title}
                onChange={handleChange}
                className="bg-gray-700 border-gray-600 text-white"
                required
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="summary">Short Summary</Label>
              <Input
                id="summary"
                name="summary"
                placeholder="A short summary of the course (displayed in cards)"
                value={courseData.summary}
                onChange={handleChange}
                className="bg-gray-700 border-gray-600 text-white"
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="description">Full Description</Label>
              <Textarea
                id="description"
                name="description"
                placeholder="Detailed description of the course content"
                value={courseData.description}
                onChange={handleChange}
                className="bg-gray-700 border-gray-600 text-white min-h-32"
              />
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="thumbnail">Thumbnail URL</Label>
              <Input
                id="thumbnail"
                name="thumbnail"
                placeholder="URL for the course thumbnail image"
                value={courseData.thumbnail}
                onChange={handleChange}
                className="bg-gray-700 border-gray-600 text-white"
              />
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="space-y-2">
                <Label htmlFor="difficulty">Difficulty Level</Label>
                <Select 
                  name="difficulty"
                  value={courseData.difficulty} 
                  onValueChange={(value) => handleSelectChange("difficulty", value)}
                >
                  <SelectTrigger className="bg-gray-700 border-gray-600 text-white">
                    <SelectValue placeholder="Select difficulty" />
                  </SelectTrigger>
                  <SelectContent className="bg-gray-800 border-gray-700 text-white">
                    <SelectItem value="BEGINNER">Beginner</SelectItem>
                    <SelectItem value="INTERMEDIATE">Intermediate</SelectItem>
                    <SelectItem value="ADVANCED">Advanced</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="estimatedHours">Estimated Hours</Label>
                <Input
                  id="estimatedHours"
                  name="estimatedHours"
                  type="number"
                  min="1"
                  max="100"
                  value={courseData.estimatedHours}
                  onChange={handleChange}
                  className="bg-gray-700 border-gray-600 text-white"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="status">Status</Label>
                <Select 
                  name="status"
                  value={courseData.status} 
                  onValueChange={(value) => handleSelectChange("status", value)}
                >
                  <SelectTrigger className="bg-gray-700 border-gray-600 text-white">
                    <SelectValue placeholder="Select status" />
                  </SelectTrigger>
                  <SelectContent className="bg-gray-800 border-gray-700 text-white">
                    <SelectItem value="DRAFT">Draft</SelectItem>
                    <SelectItem value="PUBLISHED">Published</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </CardContent>
          
          <CardFooter className="flex justify-between border-t border-gray-700 pt-6">
            <Button
              type="button"
              variant="outline"
              onClick={() => router.push("/admin/courses")}
            >
              Cancel
            </Button>
            <Button 
              type="submit"
              className="bg-indigo-600 hover:bg-indigo-700"
              disabled={isSubmitting}
            >
              {isSubmitting ? "Creating..." : "Create Course"}
            </Button>
          </CardFooter>
        </Card>
      </form>
    </div>
  )
}
