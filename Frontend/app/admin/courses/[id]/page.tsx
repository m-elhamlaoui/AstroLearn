"use client"

import { useEffect, useState, useRef } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import axios from "axios" // For direct S3 uploads
import axiosInstance from "@/lib/axiosInstance"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
// @ts-ignore
import { toast } from "react-hot-toast"
import { ArrowLeft, Edit, Plus, PlusCircle, Video, FileText, Trash2, GripVertical, Move, ArrowUpDown, Upload, Youtube } from "lucide-react"
import { DragDropContext, Droppable, Draggable, DroppableProvided as DndDroppableProvided, DraggableProvided as DndDraggableProvided, DroppableStateSnapshot, DraggableStateSnapshot } from '@hello-pangea/dnd'

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
  moduleId?: number
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
  const [editingLesson, setEditingLesson] = useState<Lesson | null>(null)
  const [isEditingLesson, setIsEditingLesson] = useState(false)
  
  // Fetch course data with modules and lessons
  useEffect(() => {
    const fetchCourseWithDetails = async () => {
      try {
        setLoading(true)
        
        // Fetch the course basic data
        const courseResponse = await axiosInstance.get(`/courses/${courseId}`)
        const courseData = courseResponse.data
        
        // Fetch modules for this course
        const modulesResponse = await axiosInstance.get(`/modules/courses/${courseId}`)
        const modulesData = modulesResponse.data || []
        
        // For each module, fetch its lessons
        const modulesWithLessons = await Promise.all(modulesData.map(async (module: any) => {
          try {
            const lessonsResponse = await axiosInstance.get(`/lessons/modules/${module.id}`)
            const lessonsData = lessonsResponse.data || []
            
            // Return the module with its lessons
            return {
              ...module,
              lessons: lessonsData
            }
          } catch (moduleErr) {
            console.error(`Error fetching lessons for module ${module.id}:`, moduleErr)
            return {
              ...module,
              lessons: []
            }
          }
        }))
        
        // Create the complete course object with modules and lessons
        const completeData = {
          ...courseData,
          modules: modulesWithLessons,
          thumbnail: courseData.imageUrl // Use the imageUrl from CourseDTO as the thumbnail
        }
        
        console.log("Complete course data with modules and lessons:", completeData)
        setCourse(completeData)
      } catch (err) {
        console.error("Error fetching course:", err)
        toast.error("Failed to load course")
      } finally {
        setLoading(false)
      }
    }

    fetchCourseWithDetails()
  }, [courseId])

  // Handle adding a new module
  const handleAddModule = async () => {
    if (!newModuleTitle.trim()) {
      toast.error("Module title is required")
      return
    }

    try {
      // Updated URL to match ModuleController endpoint
      const response = await axiosInstance.post(`/modules/courses/${courseId}`, {
        title: newModuleTitle,
        description: "",
        order: course?.modules?.length || 0
      })

      // Update local state
      setCourse(prevCourse => {
        if (!prevCourse) return null
        const oldModules = Array.isArray(prevCourse.modules) ? prevCourse.modules : [];
        return {
          ...prevCourse,
          modules: [...oldModules, response.data]
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

  // State for lesson creation per module
  const [newLessonTitles, setNewLessonTitles] = useState<{ [key: number]: string }>({});
  const [newLessonContents, setNewLessonContents] = useState<{ [key: number]: string }>({});
  const [lessonVideoType, setLessonVideoType] = useState<{ [key: number]: 's3' | 'youtube' }>({});
  const [lessonVideoFiles, setLessonVideoFiles] = useState<{ [key: number]: File | undefined }>({});
  const [lessonVideoUrls, setLessonVideoUrls] = useState<{ [key: number]: string }>({});
  const [isUploadingVideo, setIsUploadingVideo] = useState<{ [key: number]: boolean }>({});
  const fileInputRefs = useRef<{ [key: number]: HTMLInputElement | null }>({});
  
  // State to track which modules have their "Add Lesson" form expanded
  const [expandedAddLessonForms, setExpandedAddLessonForms] = useState<{ [key: number]: boolean }>({});
  
  // Function to toggle the visibility of a module's Add Lesson form
  const toggleAddLessonForm = (moduleId: number) => {
    setExpandedAddLessonForms(prev => ({
      ...prev,
      [moduleId]: !prev[moduleId]
    }));
  };

  // S3 upload logic (adapted from article-edit)
  const uploadVideoToS3 = async (moduleId: number, file: File | undefined): Promise<string> => {
    if (!file) {
      console.error("No file provided to uploadVideoToS3");
      return "";
    }
    
    setIsUploadingVideo(prev => ({ ...prev, [moduleId]: true }));
    console.log("[S3 Upload] Starting video upload for file:", file);
    
    try {
      // 1. Get pre-signed URL from your backend
      console.log("[S3 Upload] Fetching pre-signed URL from /generate-upload-url");
      const response = await axiosInstance.get<{ uploadUrl: string; key: string }>("/generate-upload-url");
      const presignedUrlData = response.data;
      console.log("[S3 Upload] Received pre-signed URL data:", presignedUrlData);
      
      if (!presignedUrlData || !presignedUrlData.uploadUrl) {
        console.error("[S3 Upload] Failed to get pre-signed URL or uploadUrl is missing.");
        throw new Error("Failed to get valid pre-signed URL.");
      }

      const actualUploadUrl = presignedUrlData.uploadUrl;

      // 2. Upload file to S3 using the pre-signed URL
      console.log(`[S3 Upload] Attempting PUT to S3 with URL: ${actualUploadUrl}`);
      console.log(`[S3 Upload] File details: name=${file.name}, size=${file.size}, type=${file.type}`);
      const s3PutHeaders = { "Content-Type": file.type };
      console.log("[S3 Upload] Headers for S3 PUT:", s3PutHeaders);
      
      await axios.put(actualUploadUrl, file, {
        headers: s3PutHeaders,
      });
      console.log("[S3 Upload] Successfully uploaded file to S3.");

      // 3. The actual URL of the uploaded video is the pre-signed URL without query parameters
      const videoUrl = actualUploadUrl.split("?")[0];
      console.log("[S3 Upload] Derived video URL for storage:", videoUrl);
      return videoUrl;
    } catch (error: any) {
      console.error("[S3 Upload] Error uploading video to S3:", error);
      toast.error(`Video upload failed: ${error.message}`);
      return "";
    } finally {
      setIsUploadingVideo(prev => ({ ...prev, [moduleId]: false }));
    }
  };
  
  // Handle file selection for video upload
  const handleVideoFileSelect = (moduleId: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setLessonVideoFiles(prev => ({ ...prev, [moduleId]: file }));
      // Set video type to S3 when a file is selected
      setLessonVideoType(prev => ({ ...prev, [moduleId]: 's3' }));
      // Clear any previously entered YouTube URL
      setLessonVideoUrls(prev => ({ ...prev, [moduleId]: "" }));
    }
  };
  
  // Handle YouTube URL input
  const handleYoutubeUrlChange = (moduleId: number, url: string) => {
    setLessonVideoUrls(prev => ({ ...prev, [moduleId]: url }));
    // Set video type to YouTube when URL is entered
    setLessonVideoType(prev => ({ ...prev, [moduleId]: 'youtube' }));
    // Clear any previously selected file
    setLessonVideoFiles(prev => ({ ...prev, [moduleId]: undefined }));
  };

  // Handle adding a new lesson to a module
  const handleAddLesson = async (moduleId: number, title: string, content: string, videoUrl: string) => {
    try {
      console.log("[Add Lesson] Starting to add lesson with data:", { moduleId, title, content, videoUrl });
      
      // Ensure moduleId is a number
      const numericModuleId = Number(moduleId);
      if (isNaN(numericModuleId)) {
        console.error("[Add Lesson] Invalid module ID format:", moduleId);
        toast.error("Invalid module ID format");
        return;
      }
      
      // Find the module in the course
      const moduleIndex = course?.modules.findIndex(m => m.id === numericModuleId);
      if (moduleIndex === -1 || moduleIndex === undefined) {
        console.error("[Add Lesson] Module not found with ID:", numericModuleId);
        console.log("[Add Lesson] Available modules:", course?.modules.map(m => m.id));
        toast.error("Module not found. Please refresh the page and try again.");
        return;
      }
      
      // Prepare the lesson data according to LessonDTO structure
      const lessonData = {
        title,
        content,
        videoUrl,
        moduleId: numericModuleId
      };
      
      console.log(`[Add Lesson] Sending POST request to /lessons/modules/${numericModuleId} with data:`, lessonData);
      
      // Make the API call to add the lesson
      const response = await axiosInstance.post(`/lessons/modules/${numericModuleId}`, lessonData);
      
      console.log("[Add Lesson] Response received:", response.data);
      
      // Update local state with the new lesson
      setCourse(prevCourse => {
        if (!prevCourse) return null;
        
        const updatedModules = [...prevCourse.modules];
        // Check if the module exists and has a lessons array
        if (updatedModules[moduleIndex] && Array.isArray(updatedModules[moduleIndex].lessons)) {
          updatedModules[moduleIndex].lessons.push(response.data);
        } else {
          // If lessons array doesn't exist, initialize it
          console.log("[Add Lesson] Initializing lessons array for module", numericModuleId);
          if (updatedModules[moduleIndex]) {
            updatedModules[moduleIndex].lessons = [response.data];
          } else {
            console.error("[Add Lesson] Module index not found in updatedModules array");
            // Log error and return unchanged state
            console.log("[Add Lesson] Will need to refresh the page to see the new lesson");
            toast.error("Lesson added but not displayed. Please refresh the page.");
            return prevCourse; // Return unchanged state for now
          }
        }
        
        return {
          ...prevCourse,
          modules: updatedModules
        };
      });
      
      // Reset lesson form for this module
      setNewLessonTitles(prev => ({ ...prev, [numericModuleId]: "" }));
      setNewLessonContents(prev => ({ ...prev, [numericModuleId]: "" }));
      setLessonVideoType(prev => ({ ...prev, [numericModuleId]: 's3' }));
      setLessonVideoFiles(prev => ({ ...prev, [numericModuleId]: undefined }));
      setLessonVideoUrls(prev => ({ ...prev, [numericModuleId]: "" }));
      
      toast.success("Lesson added successfully");
    } catch (err: any) {
      console.error("[Add Lesson] Error adding lesson:", err);
      console.error("[Add Lesson] Error details:", err.response?.data || err.message);
      toast.error(`Failed to add lesson: ${err.response?.data?.message || err.message || 'Unknown error'}`);
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
      
      // Log the auth token being used
      const authToken = localStorage.getItem('authToken')
      console.log('Module Reorder - Auth Token:', authToken ? `${authToken.substring(0, 10)}...` : 'No token found')
      
      // Log request details
      const requestUrl = `/modules/courses/${courseId}/reorder`
      const requestData = updatedModules.map(m => m.id) // Send array directly
      console.log('Module Reorder - Request URL:', requestUrl)
      console.log('Module Reorder - Request Data:', requestData)
      
      // Call API to persist the new order
      const response = await axiosInstance.put(requestUrl, requestData)
      
      // Log response
      console.log('Module Reorder - Response Status:', response.status)
      console.log('Module Reorder - Response Data:', response.data)
    } catch (err) {
      console.error("Error reordering modules:", err)
      toast.error("Failed to reorder modules")
    }
  }
  
  // Handle reordering lessons within a module
  const handleLessonReorder = async (result: any) => {
    if (!result.destination) return
    
    // Extract module ID from droppable ID (format: 'lessons-{moduleId}')
    const moduleId = parseInt(result.source.droppableId.split('-')[1])
    const fromIndex = result.source.index
    const toIndex = result.destination.index
    
    if (fromIndex === toIndex) return
    
    try {
      // Find the module containing these lessons
      const moduleIndex = course?.modules.findIndex(m => m.id === moduleId) ?? -1
      if (moduleIndex === -1 || !course) return
      
      // Get the lessons from the module
      const moduleLessons = Array.from(course.modules[moduleIndex].lessons || [])
      
      // Reorder the lessons
      const [movedLesson] = moduleLessons.splice(fromIndex, 1)
      moduleLessons.splice(toIndex, 0, movedLesson)
      
      // Update order property
      const updatedLessons = moduleLessons.map((lesson, index) => ({
        ...lesson,
        order: index
      }))
      
      // Update local state immediately for better UX
      setCourse(prevCourse => {
        if (!prevCourse) return null
        
        const updatedModules = [...prevCourse.modules]
        updatedModules[moduleIndex] = {
          ...updatedModules[moduleIndex],
          lessons: updatedLessons
        }
        
        return { ...prevCourse, modules: updatedModules }
      })
      
      // Log the auth token being used
      const authToken = localStorage.getItem('authToken')
      console.log('Lesson Reorder - Auth Token:', authToken ? `${authToken.substring(0, 10)}...` : 'No token found')
      
      // Log the axios instance headers
      console.log('Lesson Reorder - Axios Default Headers:', axiosInstance.defaults.headers)
      
      // Log request details
      const requestUrl = `/lessons/modules/${moduleId}/reorder`
      const requestData = updatedLessons.map(l => l.id) // Send array directly
      console.log('Lesson Reorder - Request URL:', requestUrl)
      console.log('Lesson Reorder - Request Data:', requestData)
      console.log('Lesson Reorder - Module ID:', moduleId)
      
      // Call API to persist the new order
      const response = await axiosInstance.put(requestUrl, requestData)
      
      // Log response
      console.log('Lesson Reorder - Response Status:', response.status)
      console.log('Lesson Reorder - Response Data:', response.data)
      
      toast.success("Lessons reordered successfully")
    } catch (err) {
      console.error("Error reordering lessons:", err)
      toast.error("Failed to reorder lessons")
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

  // Handle lesson update
  const handleUpdateLesson = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!editingLesson) return;
    
    try {
      const lessonData = {
        title: editingLesson.title,
        description: editingLesson.description,
        videoUrl: editingLesson.videoUrl,
        moduleId: editingLesson.moduleId
      };
      
      // Make API call to update the lesson
      const response = await axiosInstance.put(`/lessons/${editingLesson.id}`, lessonData);
      
      // Update local state with the updated lesson
      setCourse(prevCourse => {
        if (!prevCourse) return null;
        
        const updatedModules = [...prevCourse.modules];
        const moduleIndex = updatedModules.findIndex(m => m.id === editingLesson.moduleId);
        
        if (moduleIndex !== -1) {
          const lessonIndex = updatedModules[moduleIndex].lessons.findIndex(l => l.id === editingLesson.id);
          
          if (lessonIndex !== -1) {
            updatedModules[moduleIndex].lessons[lessonIndex] = {
              ...updatedModules[moduleIndex].lessons[lessonIndex],
              ...lessonData
            };
          }
        }
        
        return {
          ...prevCourse,
          modules: updatedModules
        };
      });
      
      // Close the edit modal
      setIsEditingLesson(false);
      setEditingLesson(null);
      
      toast.success("Lesson updated successfully");
    } catch (err: any) {
      console.error("Error updating lesson:", err);
      toast.error(`Failed to update lesson: ${err.response?.data?.message || err.message || 'Unknown error'}`);
    }
  };

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
          
          <DragDropContext onDragEnd={(result) => {
            // Check which type of item is being dragged
            if (result.type === 'lesson') {
              handleLessonReorder(result);
            } else {
              // Default to module reordering
              handleModuleReorder(result);
            }
          }}>
            <Droppable droppableId="modules" type="module">
              {(provided: DndDroppableProvided, snapshot: DroppableStateSnapshot) => (
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
                        {(provided: DndDraggableProvided, snapshot: DraggableStateSnapshot) => (
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
                                  onClick={() => {
                                    // Ensure moduleId is a number
                                    const numericModuleId = Number(module.id);
                                    if (isNaN(numericModuleId)) {
                                      console.error("[Quick Add] Invalid module ID format:", module.id);
                                      toast.error("Invalid module ID format");
                                      return;
                                    }
                                    
                                    console.log("[Quick Add] Adding quick lesson to module:", numericModuleId);
                                    
                                    // Set default values for this module before adding
                                    setNewLessonTitles(prev => ({ ...prev, [numericModuleId]: "New Lesson" }));
                                    setNewLessonContents(prev => ({ ...prev, [numericModuleId]: "" }));
                                    handleAddLesson(numericModuleId, "New Lesson", "", "");
                                  }}
                                  variant="outline"
                                  size="sm"
                                  className="text-xs"
                                >
                                  Add Quick Lesson
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
                                <Droppable droppableId={`lessons-${module.id}`} type="lesson">
                                  {(provided, snapshot) => (
                                    <div 
                                      ref={provided.innerRef}
                                      {...provided.droppableProps}
                                      className={`space-y-3 pl-6 ${snapshot.isDraggingOver ? 'bg-gray-750 rounded-md p-2' : ''}`}
                                    >
                                      {module.lessons.map((lesson, lessonIndex) => (
                                        <Draggable 
                                          key={lesson.id} 
                                          draggableId={`lesson-${lesson.id}`} 
                                          index={lessonIndex}
                                        >
                                          {(provided, snapshot) => (
                                            <div 
                                              ref={provided.innerRef}
                                              {...provided.draggableProps}
                                              className={`flex items-center justify-between p-3 bg-gray-700 rounded-md ${snapshot.isDragging ? 'ring-2 ring-indigo-500' : ''}`}
                                            >
                                              <div className="flex items-center gap-2">
                                                <div 
                                                  {...provided.dragHandleProps}
                                                  className="cursor-move text-gray-500 hover:text-gray-300 mr-1"
                                                >
                                                  <GripVertical size={16} />
                                                </div>
                                                <Video size={16} className="text-indigo-400" />
                                                <span>{lesson.title}</span>
                                              </div>
                                              <Button 
                                                variant="ghost" 
                                                size="sm"
                                                onClick={() => {
                                                  // Store lesson data in localStorage for editing
                                                  localStorage.setItem('editingLesson', JSON.stringify({
                                                    id: lesson.id,
                                                    title: lesson.title,
                                                    description: lesson.description,
                                                    videoUrl: lesson.videoUrl,
                                                    moduleId: module.id
                                                  }));
                                                  
                                                  // Open edit modal or navigate to edit view
                                                  setEditingLesson(lesson);
                                                  setIsEditingLesson(true);
                                                }}
                                                className="text-xs"
                                              >
                                                Edit
                                              </Button>
                                            </div>
                                          )}
                                        </Draggable>
                                      ))}
                                      {provided.placeholder}
                                    </div>
                                  )}
                                </Droppable>
                              ) : (
                                <p className="text-gray-400 text-sm pl-6">No lessons yet. Add a lesson to this module.</p>
                              )}
                            </CardContent>
                            <CardContent>
                              <div className="mt-4 p-4 bg-gray-900 rounded-lg">
                                <div className="flex justify-between items-center mb-3">
                                  <h4 className="text-gray-200 font-semibold">Add Lesson</h4>
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="sm"
                                    onClick={() => toggleAddLessonForm(module.id)}
                                    className="text-xs text-gray-400 hover:text-white"
                                  >
                                    {expandedAddLessonForms[module.id] ? 'Hide Form' : 'Show Form'}
                                  </Button>
                                </div>
                                {expandedAddLessonForms[module.id] && (
                                  <form
                                  onSubmit={async (e) => {
                                    e.preventDefault();
                                    console.log("[Form Submit] Form submission started for module:", module.id);
                                    
                                    // Validate lesson title
                                    if (!newLessonTitles[module.id]?.trim()) {
                                      console.log("[Form Submit] Validation failed: Title is required");
                                      toast.error("Lesson title is required");
                                      return;
                                    }
                                    
                                    let videoUrl = "";
                                    const videoType = lessonVideoType[module.id] || 's3';
                                    console.log("[Form Submit] Selected video type:", videoType);
                                    
                                    try {
                                      // Handle video based on selected type
                                      if (videoType === 's3') {
                                        if (lessonVideoFiles[module.id]) {
                                          console.log("[Form Submit] Uploading video file to S3:", lessonVideoFiles[module.id]?.name);
                                          // Upload video file to S3
                                          videoUrl = await uploadVideoToS3(module.id, lessonVideoFiles[module.id]);
                                          if (!videoUrl) {
                                            console.error("[Form Submit] S3 upload failed, returned empty URL");
                                            toast.error("Failed to upload video");
                                            return;
                                          }
                                          console.log("[Form Submit] S3 upload successful, URL:", videoUrl);
                                        }
                                        // Allow empty video URL for S3 option if no file is selected
                                      } else if (videoType === 'youtube') {
                                        // Use YouTube URL directly
                                        videoUrl = lessonVideoUrls[module.id] || "";
                                        console.log("[Form Submit] Using YouTube URL:", videoUrl);
                                        if (!videoUrl) {
                                          console.error("[Form Submit] YouTube URL is required but was empty");
                                          toast.error("YouTube URL is required");
                                          return;
                                        }
                                      }
                                      
                                      console.log("[Form Submit] Calling handleAddLesson with:", {
                                        moduleId: module.id,
                                        title: newLessonTitles[module.id],
                                        content: newLessonContents[module.id] || "",
                                        videoUrl
                                      });
                                      
                                      await handleAddLesson(
                                        module.id, 
                                        newLessonTitles[module.id], 
                                        newLessonContents[module.id] || "", 
                                        videoUrl
                                      );
                                    } catch (error: any) {
                                      console.error("[Form Submit] Error during form submission:", error);
                                      toast.error(`Form submission error: ${error.message || 'Unknown error'}`);
                                    }
                                  }}
                                >
                                  <div className="flex flex-col gap-3 mb-2">
                                    <Input
                                      placeholder="Lesson title"
                                      value={newLessonTitles[module.id] || ""}
                                      onChange={e => setNewLessonTitles({ ...newLessonTitles, [module.id]: e.target.value })}
                                      className="bg-gray-800 border-gray-700 text-white"
                                    />
                                    <Textarea
                                      placeholder="Lesson content (optional)"
                                      value={newLessonContents[module.id] || ""}
                                      onChange={e => setNewLessonContents({ ...newLessonContents, [module.id]: e.target.value })}
                                      className="bg-gray-800 border-gray-700 text-white"
                                    />
                                    
                                    {/* Video Upload Options */}
                                    <div className="mt-2">
                                      <Label className="text-sm text-gray-300 mb-2">Lesson Video</Label>
                                      <RadioGroup 
                                        value={lessonVideoType[module.id] || 's3'}
                                        onValueChange={(value) => setLessonVideoType({ 
                                          ...lessonVideoType, 
                                          [module.id]: value as 's3' | 'youtube'
                                        })}
                                        className="flex flex-col space-y-2 mt-2"
                                      >
                                        <div className="flex items-center space-x-2">
                                          <RadioGroupItem value="s3" id={`s3-${module.id}`} />
                                          <Label htmlFor={`s3-${module.id}`} className="flex items-center gap-2">
                                            <Upload size={16} />
                                            <span>Upload Video File</span>
                                          </Label>
                                        </div>
                                        <div className="flex items-center space-x-2">
                                          <RadioGroupItem value="youtube" id={`youtube-${module.id}`} />
                                          <Label htmlFor={`youtube-${module.id}`} className="flex items-center gap-2">
                                            <Youtube size={16} />
                                            <span>YouTube Video</span>
                                          </Label>
                                        </div>
                                      </RadioGroup>
                                    </div>
                                    
                                    {/* Conditional input based on selected video type */}
                                    {lessonVideoType[module.id] === 's3' ? (
                                      <div className="mt-2">
                                        <input
                                          type="file"
                                          accept="video/*"
                                          ref={(el) => {
                                            if (el) fileInputRefs.current[module.id] = el;
                                          }}
                                          onChange={(e) => handleVideoFileSelect(module.id, e)}
                                          className="bg-gray-800 border border-gray-700 text-white rounded-md p-2 w-full"
                                        />
                                        {lessonVideoFiles[module.id] && (
                                          <div className="mt-2 text-sm text-green-400 flex items-center gap-2">
                                            <Video size={16} />
                                            <span>{lessonVideoFiles[module.id]?.name}</span>
                                          </div>
                                        )}
                                      </div>
                                    ) : (
                                      <div className="mt-2">
                                        <Input
                                          placeholder="Enter YouTube URL"
                                          value={lessonVideoUrls[module.id] || ""}
                                          onChange={(e) => handleYoutubeUrlChange(module.id, e.target.value)}
                                          className="bg-gray-800 border-gray-700 text-white"
                                        />
                                      </div>
                                    )}
                                  </div>
                                  
                                  <Button
                                    type="submit"
                                    className="mt-3 bg-indigo-600 hover:bg-indigo-700 text-white w-full"
                                    disabled={isUploadingVideo[module.id]}
                                  >
                                    {isUploadingVideo[module.id] ? (
                                      <>
                                        <span className="animate-spin mr-2">⏳</span>
                                        Uploading Video...
                                      </>
                                    ) : (
                                      "Add Lesson"
                                    )}
                                  </Button>
                                </form>
                                )}
                              </div>
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
                  <div className="w-full h-full relative">
                    <img 
                      src={course.thumbnail} 
                      alt={course.title} 
                      className="w-full h-full object-cover rounded-md"
                      onError={(e) => {
                        console.error('Image failed to load:', course.thumbnail);
                        e.currentTarget.onerror = null; // Prevent infinite loop
                        e.currentTarget.style.display = 'none';
                        // Check if parentElement exists before accessing its innerHTML
                        if (e.currentTarget.parentElement) {
                          e.currentTarget.parentElement.innerHTML = '<p class="text-gray-400 absolute inset-0 flex items-center justify-center">Failed to load image</p>';
                        }
                      }}
                    />
                  </div>
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
                          <div key={lesson.id} className="space-y-1 mb-2">
                            <div className="flex items-center gap-2 text-sm text-gray-300">
                              <Video size={14} className="text-indigo-400" />
                              <span>Lesson {lessonIndex + 1}: {lesson.title}</span>
                            </div>
                            {lesson.videoUrl && (
                              <div className="ml-6 text-xs text-gray-400 flex items-center gap-1">
                                {lesson.videoUrl.includes('youtube.com') || lesson.videoUrl.includes('youtu.be') ? (
                                  <>
                                    <Youtube size={12} />
                                    <span>YouTube Video</span>
                                  </>
                                ) : (
                                  <>
                                    <Upload size={12} />
                                    <span>Uploaded Video</span>
                                  </>
                                )}
                              </div>
                            )}
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

      {/* Lesson Edit Modal */}
      {isEditingLesson && editingLesson && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-gray-800 rounded-lg shadow-lg w-full max-w-2xl overflow-hidden">
            <div className="p-6 border-b border-gray-700">
              <h3 className="text-xl font-semibold">Edit Lesson</h3>
            </div>
            
            <form onSubmit={handleUpdateLesson} className="p-6 space-y-4">
              <div className="space-y-2">
                <Label htmlFor="edit-lesson-title">Lesson Title</Label>
                <Input
                  id="edit-lesson-title"
                  value={editingLesson.title}
                  onChange={(e) => setEditingLesson({...editingLesson, title: e.target.value})}
                  className="bg-gray-700 border-gray-600 text-white"
                  required
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="edit-lesson-description">Description</Label>
                <Textarea
                  id="edit-lesson-description"
                  value={editingLesson.description}
                  onChange={(e) => setEditingLesson({...editingLesson, description: e.target.value})}
                  className="bg-gray-700 border-gray-600 text-white min-h-32"
                />
              </div>
              
              <div className="space-y-2">
                <Label htmlFor="edit-lesson-video">Video URL</Label>
                <Input
                  id="edit-lesson-video"
                  value={editingLesson.videoUrl}
                  onChange={(e) => setEditingLesson({...editingLesson, videoUrl: e.target.value})}
                  className="bg-gray-700 border-gray-600 text-white"
                  placeholder="YouTube URL or S3 video URL"
                />
              </div>
              
              <div className="flex justify-end gap-3 pt-4 border-t border-gray-700 mt-6">
                <Button 
                  type="button" 
                  variant="outline" 
                  onClick={() => {
                    setIsEditingLesson(false);
                    setEditingLesson(null);
                  }}
                >
                  Cancel
                </Button>
                <Button type="submit" className="bg-indigo-600 hover:bg-indigo-700">
                  Save Changes
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
