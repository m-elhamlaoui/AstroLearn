"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import axiosInstance from "@/lib/axiosInstance"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
// @ts-ignore
import { toast } from "react-hot-toast"
import { ArrowLeft, Edit, Plus, PlusCircle, Video, FileText, Trash2, GripVertical, Move, ArrowUpDown } from "lucide-react"
// @ts-ignore
import { DragDropContext, Droppable, Draggable } from '@hello-pangea/dnd'

// Add type declarations for DnD
type DroppableProvided = {
  innerRef: React.RefObject<HTMLDivElement>
  droppableProps: any
  placeholder?: React.ReactElement
}

type DraggableProvided = {
  innerRef: React.RefObject<HTMLDivElement>
  draggableProps: any
  dragHandleProps: any
}

interface CourseDetailsPageProps {
  params: {
    id: string
  }
}

// Define interfaces for the course content structure
interface Quiz {
  id: number
  title: string
  questions: Array<{
    id: number
    text: string
    options: Array<{
      id: number
      text: string
      isCorrect: boolean
    }>
  }>
}

interface Lesson {
  id: number
  title: string
  videoUrl: string
  description: string
  order: number
  quizzes: Quiz[]
}

interface Module {
  id: number
  title: string
  description: string
  order: number
  lessons: Lesson[]
}

interface Course {
  id: number
  title: string
  summary: string
  description: string
  thumbnail: string
  difficulty: string
  estimatedHours: number
  status: string
  modules: Module[]
}

export default function CourseDetailsPage({ params }: CourseDetailsPageProps) {
  const courseId = params.id
  const router = useRouter()
  
  const [course, setCourse] = useState<Course | null>(null)
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState("content")
  const [newModuleTitle, setNewModuleTitle] = useState("")
  const [isAddingModule, setIsAddingModule] = useState(false)
  
  // Fetch course data
  useEffect(() => {
    const fetchCourse = async () => {
      try {
        setLoading(true)
        const response = await axiosInstance.get(`/courses/${courseId}`)
        setCourse(response.data)
      } catch (err) {
        console.error("Error fetching course:", err)
        toast.error("Failed to load course")
      } finally {
        setLoading(false)
      }
    }

    fetchCourse()
  }, [courseId])

  // Handle adding a new module
  const handleAddModule = async () => {
    if (!newModuleTitle.trim()) {
      toast.error("Module title is required")
      return
    }

    try {
      const response = await axiosInstance.post(`/courses/${courseId}/modules`, {
        title: newModuleTitle,
        description: "",
        order: course?.modules?.length || 0
      })

      // Update local state
      setCourse(prevCourse => {
        if (!prevCourse) return null
        
        return {
          ...prevCourse,
          modules: [...prevCourse.modules, response.data]
        }
      })

      // Reset form
      setNewModuleTitle("")
      setIsAddingModule(false)
      toast.success("Module added successfully")
    } catch (err) {
      console.error("Error adding module:", err)
      toast.error("Failed to add module")
    }
  }

  // Handle adding a new lesson to a module
  const handleAddLesson = async (moduleId: number) => {
    try {
      const moduleIndex = course?.modules.findIndex(m => m.id === moduleId) || -1
      if (moduleIndex === -1) return
      
      const response = await axiosInstance.post(`/courses/${courseId}/modules/${moduleId}/lessons`, {
        title: "New Lesson",
        videoUrl: "",
        description: "",
        order: course?.modules[moduleIndex].lessons.length || 0
      })

      // Update local state
      setCourse(prevCourse => {
        if (!prevCourse) return null
        
        const updatedModules = [...prevCourse.modules]
        updatedModules[moduleIndex].lessons.push(response.data)
        
        return {
          ...prevCourse,
          modules: updatedModules
        }
      })
      
      toast.success("Lesson added successfully")
    } catch (err) {
      console.error("Error adding lesson:", err)
      toast.error("Failed to add lesson")
    }
  }

  // Handle reordering modules
  const handleModuleReorder = async (result: any) => {
    if (!result.destination) return
    
    const fromIndex = result.source.index
    const toIndex = result.destination.index
    
    if (fromIndex === toIndex) return
    
    try {
      const reorderedModules = Array.from(course?.modules || [])
      const [movedModule] = reorderedModules.splice(fromIndex, 1)
      reorderedModules.splice(toIndex, 0, movedModule)
      
      // Update order property
      const updatedModules = reorderedModules.map((module, index) => ({
        ...module,
        order: index
      }))
      
      // Update local state immediately for better UX
      setCourse(prevCourse => {
        if (!prevCourse) return null
        return { ...prevCourse, modules: updatedModules }
      })
      
      // Call API to persist the new order
      await axiosInstance.put(`/courses/${courseId}/modules/reorder`, {
        moduleIds: updatedModules.map(m => m.id)
      })
    } catch (err) {
      console.error("Error reordering modules:", err)
      toast.error("Failed to reorder modules")
    }
  }

  // Handle deleting a module
  const handleDeleteModule = async (moduleId: number) => {
    if (!confirm("Are you sure you want to delete this module? This will also delete all lessons and quizzes inside it.")) {
      return
    }
    
    try {
      await axiosInstance.delete(`/courses/${courseId}/modules/${moduleId}`)
      
      // Update local state
      setCourse(prevCourse => {
        if (!prevCourse) return null
        
        return {
          ...prevCourse,
          modules: prevCourse.modules.filter(m => m.id !== moduleId)
        }
      })
      
      toast.success("Module deleted successfully")
    } catch (err) {
      console.error("Error deleting module:", err)
      toast.error("Failed to delete module")
    }
  }

  if (loading) {
    return (
      <div className="flex items-center justify-center min-h-[50vh]">
        <div className="w-16 h-16 border-4 border-t-indigo-500 border-r-transparent border-b-indigo-500 border-l-transparent rounded-full animate-spin"></div>
      </div>
    )
  }

  if (!course) {
    return (
      <div className="text-center py-10">
        <h2 className="text-2xl font-bold text-red-500">Error</h2>
        <p className="mt-2">Could not load course details</p>
        <Button 
          onClick={() => router.push("/admin/courses")}
          className="mt-4 px-4 py-2 bg-indigo-600 hover:bg-indigo-700"
        >
          Back to Courses
        </Button>
      </div>
    )
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
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <h1 className="text-3xl font-bold tracking-tight">{course.title}</h1>
            <Button
              onClick={() => router.push(`/admin/courses/${courseId}/edit`)}
              className="flex items-center gap-1"
            >
              <Edit size={16} />
              <span>Edit Course</span>
            </Button>
          </div>
          <p className="text-gray-400 mt-2">
            {course.status === "PUBLISHED" ? (
              <span className="bg-green-600 text-white text-xs px-2 py-1 rounded">PUBLISHED</span>
            ) : (
              <span className="bg-amber-600 text-white text-xs px-2 py-1 rounded">DRAFT</span>
            )}
            <span className="ml-2">{course.difficulty} • {course.estimatedHours} hours</span>
          </p>
        </div>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
        <TabsList className="bg-gray-800 border-gray-700">
          <TabsTrigger value="content">Course Content</TabsTrigger>
          <TabsTrigger value="preview">Preview</TabsTrigger>
        </TabsList>
        
        <TabsContent value="content" className="space-y-6">
          <div className="flex justify-between items-center">
            <h2 className="text-2xl font-semibold">Modules</h2>
          </div>
          
          <DragDropContext onDragEnd={handleModuleReorder}>
            <Droppable droppableId="modules">
              {(provided: DroppableProvided) => (
                <div
                  {...provided.droppableProps}
                  ref={provided.innerRef}
                  className="space-y-4"
                >
                  {course.modules && course.modules.length > 0 ? (
                    course.modules.map((module, index) => (
                      <Draggable 
                        key={module.id} 
                        draggableId={`module-${module.id}`} 
                        index={index}
                      >
                        {(provided: DraggableProvided) => (
                          <Card 
                            ref={provided.innerRef}
                            {...provided.draggableProps}
                            className="bg-gray-800 border-gray-700"
                          >
                            <CardHeader className="flex flex-row items-center justify-between pb-2">
                              <div className="flex items-center gap-3">
                                <div 
                                  {...provided.dragHandleProps}
                                  className="cursor-move text-gray-500 hover:text-gray-300"
                                >
                                  <GripVertical size={20} />
                                </div>
                                <CardTitle className="text-xl text-white">
                                  {module.title}
                                </CardTitle>
                              </div>
                              <div className="flex items-center gap-2">
                                <Button 
                                  variant="outline" 
                                  size="sm"
                                  onClick={() => handleAddLesson(module.id)}
                                  className="text-xs"
                                >
                                  Add Lesson
                                </Button>
                                <Button 
                                  variant="ghost" 
                                  size="sm"
                                  onClick={() => handleDeleteModule(module.id)}
                                  className="text-xs text-red-500 hover:text-red-600 hover:bg-gray-700"
                                >
                                  <Trash2 size={16} />
                                </Button>
                              </div>
                            </CardHeader>
                            <CardContent>
                              {module.lessons && module.lessons.length > 0 ? (
                                <div className="space-y-3 pl-6">
                                  {module.lessons.map((lesson) => (
                                    <div 
                                      key={lesson.id}
                                      className="flex items-center justify-between p-3 bg-gray-700 rounded-md"
                                    >
                                      <div className="flex items-center gap-2">
                                        <Video size={16} className="text-indigo-400" />
                                        <span>{lesson.title}</span>
                                      </div>
                                      <Button 
                                        variant="ghost" 
                                        size="sm"
                                        onClick={() => router.push(`/admin/courses/${courseId}/modules/${module.id}/lessons/${lesson.id}`)}
                                        className="text-xs"
                                      >
                                        Edit
                                      </Button>
                                    </div>
                                  ))}
                                </div>
                              ) : (
                                <p className="text-gray-400 text-sm pl-6">No lessons yet. Add a lesson to this module.</p>
                              )}
                            </CardContent>
                          </Card>
                        )}
                      </Draggable>
                    ))
                  ) : (
                    <Card className="bg-gray-800 border-gray-700 p-6">
                      <div className="text-center py-8">
                        <p className="text-gray-400 mb-4">No modules yet. Add your first module to get started.</p>
                      </div>
                    </Card>
                  )}
                  {provided.placeholder}
                </div>
              )}
            </Droppable>
          </DragDropContext>
          
          {isAddingModule ? (
            <Card className="bg-gray-800 border-gray-700">
              <CardContent className="pt-6">
                <div className="space-y-4">
                  <div className="space-y-2">
                    <Label htmlFor="newModuleTitle">Module Title</Label>
                    <Input
                      id="newModuleTitle"
                      placeholder="Enter module title"
                      value={newModuleTitle}
                      onChange={(e) => setNewModuleTitle(e.target.value)}
                      className="bg-gray-700 border-gray-600 text-white"
                    />
                  </div>
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      onClick={() => {
                        setIsAddingModule(false)
                        setNewModuleTitle("")
                      }}
                    >
                      Cancel
                    </Button>
                    <Button
                      onClick={handleAddModule}
                      className="bg-indigo-600 hover:bg-indigo-700"
                    >
                      Add Module
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          ) : (
            <Button
              onClick={() => setIsAddingModule(true)}
              className="w-full py-6 flex items-center justify-center gap-2 border-2 border-dashed border-gray-700 bg-gray-800 hover:bg-gray-750 text-indigo-400 hover:text-indigo-300 hover:border-gray-600"
            >
              <PlusCircle size={20} />
              <span>Add New Module</span>
            </Button>
          )}
        </TabsContent>
        
        <TabsContent value="preview" className="space-y-4">
          <Card className="bg-gray-800 border-gray-700">
            <CardContent className="p-6">
              <div className="aspect-video bg-gray-700 rounded-md flex items-center justify-center mb-4">
                {course.thumbnail ? (
                  <img 
                    src={course.thumbnail} 
                    alt={course.title} 
                    className="w-full h-full object-cover rounded-md"
                  />
                ) : (
                  <p className="text-gray-400">No thumbnail image</p>
                )}
              </div>
              
              <h2 className="text-2xl font-bold mb-2">{course.title}</h2>
              
              <div className="flex gap-2 mb-4">
                <span className="bg-indigo-600 text-white text-xs px-2 py-1 rounded">{course.difficulty}</span>
                <span className="bg-gray-700 text-white text-xs px-2 py-1 rounded">{course.estimatedHours} hours</span>
              </div>
              
              <p className="text-gray-300 mb-4">{course.summary}</p>
              
              <div className="border-t border-gray-700 pt-4">
                <h3 className="text-lg font-semibold mb-2">Description</h3>
                <p className="text-gray-300 whitespace-pre-line">{course.description}</p>
              </div>
            </CardContent>
          </Card>
          
          <Card className="bg-gray-800 border-gray-700">
            <CardHeader>
              <CardTitle>Course Content</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {course.modules && course.modules.length > 0 ? (
                course.modules.map((module, index) => (
                  <div key={module.id} className="space-y-2">
                    <div className="font-semibold">Module {index + 1}: {module.title}</div>
                    <div className="pl-4 space-y-1">
                      {module.lessons && module.lessons.length > 0 ? (
                        module.lessons.map((lesson, lessonIndex) => (
                          <div key={lesson.id} className="flex items-center gap-2 text-sm text-gray-300">
                            <Video size={14} className="text-indigo-400" />
                            <span>Lesson {lessonIndex + 1}: {lesson.title}</span>
                          </div>
                        ))
                      ) : (
                        <p className="text-sm text-gray-400">No lessons</p>
                      )}
                    </div>
                  </div>
                ))
              ) : (
                <p className="text-gray-400">No content added yet</p>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  )
}
