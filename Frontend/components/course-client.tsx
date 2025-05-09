"use client"

import { useState, useEffect } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion"
import { Star, Users, Clock, ArrowLeft, CheckCircle, Play } from "lucide-react"
import Link from "next/link"
import Image from "next/image"
import axiosInstance from "@/lib/axiosInstance"
import { useParams } from 'next/navigation';

// Interfaces based on dtos.txt
interface CourseDTO {
  id: number;
  title: string;
  imageUrl: string;
  description: string;
  difficulty: string; // Assuming DifficultyLevel is a string like "BEGINNER", "INTERMEDIATE", "ADVANCED"
  totalLessons: number;
  moduleIds: number[];
  // --- Fields not in backend DTO, add defaults or handle optionality ---
  instructor?: string;
  level?: string; // Use difficulty instead?
  duration?: string;
  rating?: number;
  studentsCount?: number;
  completed?: boolean; // This likely comes from CourseProgressDTO
  longDescription?: string; // Not in DTO
  instructorBio?: string; // Not in DTO
  instructorImage?: string; // Not in DTO
}

interface ModuleDTO {
  id: number;
  title: string;
  courseId: number;
  lessonCount: number;
  lessonIds: number[];
  // --- Fields not in backend DTO ---
  description?: string; // Add if needed, otherwise remove usage
}

interface LessonDTO {
  id: number;
  title: string;
  content: string;
  videoUrl: string;
  moduleId: number;
  quizId: number | null; // quizId can be null
  // --- Fields not in backend DTO ---
  duration?: string;
  completed?: boolean; // This likely comes from CourseProgressDTO
}

// TODO: Fetch CourseProgressDTO for progress tracking
// interface CourseProgressDTO {
//   id: number;
//   completionPercentage: number;
//   completed: boolean;
//   lastAccessed: string; // Assuming LocalDateTime maps to string
//   userId: number;
//   courseId: number;
//   currentLessonId: number | null;
//   completedLessonIds: number[];
// }

const defaultImage = "/placeholder.svg?height=500&width=1000";
const defaultInstructorImage = "/placeholder.svg?height=100&width=100";

export default function CourseClient() {
  const params = useParams<{ id: string }>();
  const courseId = params?.id;

  const [course, setCourse] = useState<CourseDTO | null>(null);
  const [modules, setModules] = useState<ModuleDTO[]>([]);
  const [lessons, setLessons] = useState<LessonDTO[]>([]);
  const [expandedModules, setExpandedModules] = useState<string[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // Fetch Course Details
  useEffect(() => {
    if (!courseId) return;
    setLoading(true);
    setError(null);
    const fetchCourse = async () => {
      try {
        const response = await axiosInstance.get(`/courses/${courseId}`);
        setCourse(response.data);
      } catch (err) {
        console.error("Error fetching course:", err);
        setError("Failed to load course details.");
      }
    };
    fetchCourse();
  }, [courseId]);

  // Fetch Modules when course is loaded
  useEffect(() => {
    if (!course) return;
    const fetchModules = async () => {
      try {
        const response = await axiosInstance.get(`/modules/courses/${course.id}`);
        setModules(response.data);
      } catch (err) {
        console.error("Error fetching modules:", err);
        setError("Failed to load course modules.");
      }
    };
    fetchModules();
  }, [course]);

  // Fetch Lessons when modules are loaded
  useEffect(() => {
    if (modules.length === 0) return;
    const fetchAllLessons = async () => {
      try {
        const lessonPromises = modules.map(module =>
          axiosInstance.get(`/lessons/modules/${module.id}`)
        );
        const lessonResponses = await Promise.all(lessonPromises);
        const allLessons = lessonResponses.map(res => res.data).flat();
        setLessons(allLessons);
        setLoading(false); // Stop loading only after all data is fetched
      } catch (err) {
        console.error("Error fetching lessons:", err);
        setError("Failed to load course lessons.");
        setLoading(false);
      }
    };
    fetchAllLessons();
  }, [modules]);

  // --- UI Rendering ---

  if (loading) {
    return <div>Loading...</div>; // Or a proper skeleton loader
  }

  if (error) {
    return <div className="text-red-500 p-4">{error}</div>;
  }

  if (!course) {
    return <div>Course not found.</div>;
  }

  // TODO: Implement progress calculation based on CourseProgressDTO
  const progressPercentage = 0;
  const completedLessonsCount = 0; // Placeholder

  // TODO: Implement logic based on CourseProgressDTO
  const startLessonLink = modules.length > 0 && lessons.length > 0
    ? `/courses/${course.id}/modules/${modules[0].id}/lessons/${lessons[0].id}`
    : `/courses/${course.id}`; // Fallback link

  return (
    <div className="flex min-h-screen bg-black text-white">
      <MinimalNavigation />
      <main className="flex-1 p-6 ml-12 transition-all duration-300">
        <div className="container mx-auto max-w-5xl">
          <Link href="/courses" className="inline-flex items-center gap-2 text-gray-400 hover:text-white mb-6">
            <ArrowLeft className="h-4 w-4" />
            Back to Courses
          </Link>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-10">
            <div className="md:col-span-1">
              <div className="relative h-60 rounded-xl overflow-hidden">
                <Image src={course.imageUrl || defaultImage} alt={course.title} fill className="object-cover" />
              </div>
            </div>
            <div className="md:col-span-2">
              <h1 className="text-3xl font-bold mb-4">{course.title}</h1>
              <p className="text-gray-300 mb-4">{course.description}</p>
              <div className="flex flex-wrap gap-4 mb-4">
                <Badge className="bg-gray-800 text-white px-3 py-1">{course.difficulty || 'N/A'}</Badge>
                {/* Duration not in CourseDTO, maybe calculate from lessons? */}
                {/* <Badge className="bg-gray-800 text-white px-3 py-1">{course.duration || 'N/A'}</Badge> */}
              </div>
              <div className="flex flex-wrap gap-6 mb-6">
                <div className="flex items-center gap-1">
                  <Star className="h-5 w-5 text-yellow-500 fill-yellow-500" />
                  {/* Rating not in CourseDTO */}
                  <span className="font-medium">{course.rating ?? 'N/A'}</span>
                </div>
                <div className="flex items-center gap-1 text-gray-300">
                  <Users className="h-5 w-5" />
                  {/* studentsCount not in CourseDTO */}
                  <span>{course.studentsCount?.toLocaleString() ?? 'N/A'} students</span>
                </div>
                {/* <div className="flex items-center gap-1 text-gray-300">
                  <Clock className="h-5 w-5" />
                  <span>{course.duration || 'N/A'}</span>
                </div> */}
              </div>
              <div className="mb-6">
                <div className="flex justify-between text-sm mb-1">
                  <span>Course Progress</span>
                  <span>{progressPercentage}% Complete</span>
                </div>
                <div className="w-full bg-gray-800 rounded-full h-2.5">
                  <div className="bg-green-600 h-2.5 rounded-full" style={{ width: `${progressPercentage}%` }}></div>
                </div>
              </div>
              <Link href={startLessonLink}>
                <Button className="bg-white text-black hover:bg-gray-200 px-6 py-2 rounded-lg">
                  <Play className="h-4 w-4 mr-2" />
                  {/* Logic depends on CourseProgressDTO */}
                  {completedLessonsCount > 0 ? "Continue Course" : "Start Course"}
                </Button>
              </Link>
            </div>
          </div>

          <div className="bg-gray-900 rounded-xl p-6 mb-10">
             {/* Instructor info not available in CourseDTO */}
             <h3 className="text-xl font-bold mb-1">Instructor: {course.instructor || 'Not Available'}</h3>
          </div>

          <div className="mb-10">
            <h2 className="text-2xl font-bold mb-4">About This Course</h2>
            {/* Use description from DTO, longDescription is not available */}
            <div className="prose prose-invert max-w-none">{course.description}</div>
          </div>

          <div>
            <h2 className="text-2xl font-bold mb-4">Course Content</h2>
            <div className="bg-gray-900 rounded-xl overflow-hidden">
              <Accordion type="multiple" value={expandedModules} onValueChange={setExpandedModules} className="w-full">
                {modules.map((module) => {
                  // const completedModuleLessons = lessons.filter(l => l.moduleId === module.id && l.completed).length; // Needs progress data
                  const completedModuleLessons = 0; // Placeholder
                  const totalModuleLessons = module.lessonCount;
                  const moduleProgress = totalModuleLessons > 0 ? Math.round((completedModuleLessons / totalModuleLessons) * 100) : 0;

                  return (
                    <AccordionItem key={module.id} value={`module-${module.id}`} className="border-b border-gray-800 last:border-0">
                      <AccordionTrigger className="px-6 py-4 hover:bg-gray-800 transition-colors">
                        <div className="flex-1 text-left">
                          <div className="flex justify-between items-center">
                            <h3 className="text-lg font-medium">Module {module.id}: {module.title}</h3>
                            <div className="text-sm text-gray-400">{completedModuleLessons}/{totalModuleLessons} lessons</div>
                          </div>
                          {/* Module description not in DTO */}
                          {/* <p className="text-sm text-gray-400 mt-1">{module.description}</p> */}
                          <div className="w-full bg-gray-800 rounded-full h-1.5 mt-2">
                            <div className="bg-green-600 h-1.5 rounded-full" style={{ width: `${moduleProgress}%` }}></div>
                          </div>
                        </div>
                      </AccordionTrigger>
                      <AccordionContent className="px-6 py-2">
                        <ul className="space-y-2">
                          {lessons.filter(lesson => lesson.moduleId === module.id).map((lesson) => (
                            <li key={lesson.id}>
                              <Link href={`/courses/${course.id}/modules/${module.id}/lessons/${lesson.id}`} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-800 transition-colors">
                                <div className="flex items-center gap-3">
                                  {/* Lesson completion status not available */}
                                  <Play className="h-5 w-5 text-gray-400" />
                                  <span className="text-white">{lesson.title}</span>
                                </div>
                                {/* Lesson duration not available */}
                                {/* <span className="text-sm text-gray-400">{lesson.duration || 'N/A'}</span> */}
                              </Link>
                            </li>
                          ))}
                        </ul>
                      </AccordionContent>
                    </AccordionItem>
                  );
                })}
              </Accordion>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}
