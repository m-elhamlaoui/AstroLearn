"use client"

import { useState, useEffect, use } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import QuizDisplay from "@/components/quiz/QuizDisplay" 
import QuizReviewDisplay from "@/components/quiz/QuizReviewDisplay" 
import { Button } from "@/components/ui/button"
import { ArrowLeft, ArrowRight, CheckCircle, Award, AlertTriangle, RotateCcw } from "lucide-react" 
import Link from "next/link"
import { useRouter } from "next/navigation"
import axiosInstance from "@/lib/axiosInstance"

// DTO interfaces
interface LessonDTO {
  id: number
  title: string
  content: string
  videoUrl: string | null
  moduleId: number
  quizId: number | null
}

interface QuizQuestionDTO {
  id: number
  questionText: string
  options: string[]
}

interface QuizDTO {
  id: number
  title: string
  questions: QuizQuestionDTO[]
  lessonId: number
  lessonTitle: string
  experienceReward: number
}

interface QuizQuestionAnswerDTO {
  questionId: number
  chosenOptionIndex: number
}

interface QuizSubmissionPayload {
  answers: QuizQuestionAnswerDTO[];
}

interface QuizAttemptDetail {
  questionId: number;
  questionText: string; 
  options: string[];    
  chosenOptionIndex: number;
  correctOptionIndex: number;
  isCorrect: boolean; 
}

interface AugmentedQuizCompletionDTO {
  id: number; 
  rawScore: number; 
  totalQuestions: number; 
  completionDate: string; 
  userId: number;
  username: string;
  quizId: number;
  quizTitle: string;
  experienceEarned: number; 
  attemptDetails: QuizAttemptDetail[]; 
}

interface QuizSubmissionResultDTO {
  completionId: number;
  rawScore: number;
  totalQuestions: number;
  isPerfected: boolean;
  experienceEarned: number;
}

type QuizProgressState = 'unstarted' | 'active' | 'reviewing' | 'perfected' | 'show_score_retake';

interface NavLesson {
  courseId: string
  moduleId: string
  lessonId: string
}

interface CourseProgressDTO {
  completedLessonIds: number[]
}

interface LessonPageParams {
  id: string 
  moduleId: string
  lessonId: string
}

export default function LessonPage({ params: paramsPromise }: { params: Promise<LessonPageParams> }) {
  const params = use(paramsPromise)
  const router = useRouter()

  const [lessonDetails, setLessonDetails] = useState<LessonDTO | null>(null)
  const [quizDetails, setQuizDetails] = useState<QuizDTO | null>(null)
  const [quizReviewData, setQuizReviewData] = useState<AugmentedQuizCompletionDTO | null>(null)
  const [currentCompletionId, setCurrentCompletionId] = useState<number | null>(null);
  const [quizProgressState, setQuizProgressState] = useState<QuizProgressState>('unstarted')
  const [quizAttemptKey, setQuizAttemptKey] = useState(0) 
  const [isCompleted, setIsCompleted] = useState(false) 
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmittingQuiz, setIsSubmittingQuiz] = useState(false) 
  const [error, setError] = useState<string | null>(null) 
  const [quizSpecificError, setQuizSpecificError] = useState<string | null>(null) 
  const [nextLesson, setNextLesson] = useState<NavLesson | null>(null)
  const [prevLesson, setPrevLesson] = useState<NavLesson | null>(null)

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
      setQuizSpecificError(null) 
      setLessonDetails(null)
      setQuizDetails(null)
      setQuizReviewData(null)    
      setQuizProgressState('unstarted') 

      try {
        const lessonResponse = await axiosInstance.get<LessonDTO>(`/lessons/${params.lessonId}`)
        const currentLesson = lessonResponse.data
        setLessonDetails(currentLesson)

        let lessonAlreadyMarkedCompletedInSystem = false
        try {
          const progressResponse = await axiosInstance.get<CourseProgressDTO>(
            `/course-progress/${userIdForProgress}/${params.id}`,
          )
          const completedIds = new Set(progressResponse.data.completedLessonIds || [])
          lessonAlreadyMarkedCompletedInSystem = completedIds.has(Number(params.lessonId))
          setIsCompleted(lessonAlreadyMarkedCompletedInSystem)
        } catch (progressError) {
          console.warn("Failed to fetch course progress:", progressError)
        }

        if (currentLesson?.quizId) {
          try {
            const quizResponse = await axiosInstance.get<QuizDTO>(`/quizzes/lessons/${currentLesson.id}`)
            setQuizDetails(quizResponse.data)

            if (lessonAlreadyMarkedCompletedInSystem) {
              try {
                const completionResponse = await axiosInstance.get<AugmentedQuizCompletionDTO>(
                  `/quizzes/${currentLesson.quizId}/completion?userId=${userIdForProgress}`
                )
                if (completionResponse.data && completionResponse.data.rawScore === completionResponse.data.totalQuestions) {
                  setQuizReviewData(completionResponse.data)
                  setQuizProgressState('perfected') 
                } else {
                  setQuizProgressState('unstarted')
                }
              } catch (completionFetchError) {
                console.warn("Failed to fetch prior detailed quiz completion data:", completionFetchError)
                setQuizProgressState('unstarted') 
              }
            } else {
              setQuizProgressState('unstarted')
            }
          } catch (quizFetchError) {
            console.error("Failed to load quiz details:", quizFetchError)
            setQuizSpecificError("Could not load quiz details.")
          }
        } else if (currentLesson && !currentLesson.quizId && !lessonAlreadyMarkedCompletedInSystem) {
          try {
            await axiosInstance.post(
              `/course-progress/${userIdForProgress}/${params.id}/lessons/${params.lessonId}/complete`,
            )
            setIsCompleted(true)
          } catch (completionError) {
            console.warn("Failed to mark lesson (no quiz) as complete:", completionError)
          }
        }
        
        const courseModulesResponse = await axiosInstance.get<
          { id: number; title: string; lessonIds: number[] }[]
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
          setPrevLesson(currentLessonIndex > 0 ? allLessonsInCourseFlat[currentLessonIndex - 1] : null)
          setNextLesson(currentLessonIndex < allLessonsInCourseFlat.length - 1 ? allLessonsInCourseFlat[currentLessonIndex + 1] : null)
        } else {
          setPrevLesson(null)
          setNextLesson(null)
        }

      } catch (err) {
        console.error("Failed to load lesson data:", err)
        setError("Failed to load lesson. Please try again later.")
      } finally {
        setIsLoading(false)
      }
    }

    fetchLessonAllData()
  }, [params.id, params.moduleId, params.lessonId, userIdForProgress])

  useEffect(() => {
    const fetchReviewData = async () => {
      if (!currentCompletionId || quizProgressState !== 'reviewing' || !lessonDetails) return;
      if (quizReviewData && quizReviewData.id === currentCompletionId) return;

      setQuizSpecificError(null);
      try {
        const reviewResponse = await axiosInstance.get<AugmentedQuizCompletionDTO>(
          `/quizzes/completions/${currentCompletionId}` 
        );
        setQuizReviewData(reviewResponse.data);

        if (reviewResponse.data.rawScore === reviewResponse.data.totalQuestions) {
          setQuizProgressState('perfected'); 
          if (!isCompleted) {
            try {
              await axiosInstance.post(
                `/course-progress/${userIdForProgress}/${params.id}/lessons/${lessonDetails.id}/complete`
              );
              setIsCompleted(true);
            } catch (lessonCompleteError) {
              console.warn(
                "Failed to mark lesson complete post-review fetch:",
                lessonCompleteError
              );
            }
          }
        }
      } catch (fetchErr) {
        console.error("Failed to fetch review data:", fetchErr);
        setQuizSpecificError("Could not load your quiz results for review.");
      }
    };

    fetchReviewData();
  }, [currentCompletionId, quizProgressState, lessonDetails, userIdForProgress, params.id, isCompleted, quizReviewData]);

  const handleNavigation = (navLesson: NavLesson | null) => {
    if (navLesson) {
      router.push(`/courses/${navLesson.courseId}/modules/${navLesson.moduleId}/lessons/${navLesson.lessonId}`)
    } else {
      router.push(`/courses/${params.id}`)
    }
  }

  const handleQuizSubmit = async (answers: Map<number, number>) => {
    if (!quizDetails || !lessonDetails) return

    setIsSubmittingQuiz(true)
    setQuizSpecificError(null)

    const submissionAnswers: QuizQuestionAnswerDTO[] = []
    answers.forEach((chosenOptionIndex, questionId) => {
      submissionAnswers.push({ questionId, chosenOptionIndex })
    })

    const submissionPayload: QuizSubmissionPayload = {
      answers: submissionAnswers,
    }

    try {
      const response = await axiosInstance.post<QuizSubmissionResultDTO>(
        `/quizzes/${quizDetails.id}/submit?userId=${userIdForProgress}`,
        submissionPayload,
      );
      const result = response.data;
      setCurrentCompletionId(result.completionId); 

      if (result.isPerfected) {
        setQuizProgressState('perfected');
        setQuizReviewData({ 
            id: result.completionId,
            rawScore: result.rawScore,
            totalQuestions: result.totalQuestions,
            completionDate: new Date().toISOString(), 
            userId: userIdForProgress,
            username: "User", 
            quizId: quizDetails.id,
            quizTitle: quizDetails.title,
            experienceEarned: result.experienceEarned,
            attemptDetails: [] 
        });

        if (!isCompleted) {
          try {
            await axiosInstance.post(
              `/course-progress/${userIdForProgress}/${params.id}/lessons/${lessonDetails.id}/complete`,
            );
            setIsCompleted(true);
          } catch (lessonCompleteError) {
            console.warn("Failed to mark lesson as complete after 100% quiz score:", lessonCompleteError);
          }
        }
      } else {
        setQuizProgressState('show_score_retake');
        setQuizReviewData({ 
            id: result.completionId,
            rawScore: result.rawScore,
            totalQuestions: result.totalQuestions,
            completionDate: new Date().toISOString(),
            userId: userIdForProgress,
            username: "User", 
            quizId: quizDetails.id,
            quizTitle: quizDetails.title,
            experienceEarned: result.experienceEarned, 
            attemptDetails: [] 
        });
      }
    } catch (err: any) {
      console.error("Quiz submission failed:", err);
      if (err.response) {
        console.error("Backend Error Response:", err.response.data);
      }
      const errorMessage = err.response?.data?.message || "Failed to submit quiz. Please try again."
      setQuizSpecificError(errorMessage)
    } finally {
      setIsSubmittingQuiz(false)
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

  const renderVideoPlayer = () => {
    if (lessonDetails.videoUrl) {
      let videoSrc = lessonDetails.videoUrl;
      if (videoSrc.includes("youtube.com/watch?v=")) {
        const videoId = videoSrc.split("watch?v=")[1].split("&")[0];
        videoSrc = `https://www.youtube.com/embed/${videoId}`;
      } else if (videoSrc.includes("youtu.be/")) {
        const videoId = videoSrc.split("youtu.be/")[1].split("?")[0];
        videoSrc = `https://www.youtube.com/embed/${videoId}`;
      }

      return (
        <div className="mb-8">
          <div className="relative pb-[56.25%] h-0 overflow-hidden rounded-xl shadow-2xl">
            <iframe
              src={videoSrc}
              className="absolute top-0 left-0 w-full h-full"
              title={lessonDetails.title}
              frameBorder="0"
              allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
              allowFullScreen
            ></iframe>
          </div>
        </div>
      );
    }
    return null;
  };

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

          {renderVideoPlayer()}

          <div className="mb-10 prose prose-invert max-w-none bg-gray-900/50 p-6 rounded-xl" 
               dangerouslySetInnerHTML={{ __html: lessonDetails.content }} />

          {/* Quiz Section Logic based on quizProgressState */}
          {quizDetails && quizProgressState === 'unstarted' && (
            <div className="mt-10 text-center">
              <Button 
                onClick={() => setQuizProgressState('active')}
                className="bg-yellow-500 hover:bg-yellow-600 text-black font-semibold py-3 px-6 text-lg"
              >
                Start Quiz: {quizDetails.title}
              </Button>
            </div>
          )}

          {quizDetails && quizProgressState === 'active' && (
            <QuizDisplay
              key={quizAttemptKey} 
              quiz={quizDetails}
              onQuizSubmit={handleQuizSubmit}
              isSubmitting={isSubmittingQuiz}
              initialAnswers={new Map()} 
            />
          )}

          {quizProgressState === 'reviewing' && (
            <>
              {!quizReviewData && currentCompletionId && (
                <div className="mt-10 text-center p-6 bg-gray-800 rounded-xl">
                  <p className="text-lg mb-4">Loading your results...</p>
                </div>
              )}
              {quizReviewData && (
                 <>
                   <QuizReviewDisplay reviewData={quizReviewData} />
                   <div className="mt-6 text-center">
                     {quizReviewData.rawScore !== quizReviewData.totalQuestions && (
                       <Button
                         onClick={() => {
                           setQuizProgressState('active');
                           setQuizAttemptKey(prevKey => prevKey + 1); 
                           setQuizSpecificError(null); 
                           setCurrentCompletionId(null); 
                           setQuizReviewData(null); 
                         }}
                         className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 px-6 text-lg"
                       >
                         <RotateCcw className="h-5 w-5 mr-2" />
                         Retake Quiz
                       </Button>
                     )}
                      {quizReviewData.rawScore === quizReviewData.totalQuestions && (
                         <div className="mt-4 p-4 bg-green-900/70 border border-green-600 rounded-xl shadow-md text-center">
                           <Award className="h-8 w-8 text-yellow-400 mx-auto mb-2" />
                           <p className="text-xl font-semibold text-green-200">Quiz Perfected!</p>
                           <p className="text-md text-gray-300">
                             Score: {quizReviewData.rawScore}/{quizReviewData.totalQuestions}
                           </p>
                           {isCompleted && (
                             <div className="flex items-center justify-center gap-1 text-sm text-green-400 mt-1">
                               <CheckCircle className="h-4 w-4" />
                               <span>Lesson marked as completed!</span>
                             </div>
                           )}
                         </div>
                      )}
                   </div>
                 </>
              )}
            </>
          )}

          {quizProgressState === 'show_score_retake' && quizReviewData && (
            <div className="mt-10 p-6 bg-gray-800 rounded-xl text-center">
              <h2 className="text-2xl font-bold mb-3 text-yellow-400">Try Again!</h2>
              <p className="text-xl text-gray-100 mb-2">
                Your score: <span className="font-semibold">{quizReviewData.rawScore}</span> / {quizReviewData.totalQuestions}
              </p>
              <p className="text-md text-gray-300 mb-6">
                You earned {quizReviewData.experienceEarned || 0} XP for this attempt. Keep trying to perfect it!
              </p>
              <Button
                onClick={() => {
                  setQuizProgressState('active');
                  setQuizAttemptKey(prevKey => prevKey + 1);
                  setQuizSpecificError(null);
                  setCurrentCompletionId(null); 
                  setQuizReviewData(null); 
                }}
                className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 px-6 text-lg"
              >
                <RotateCcw className="h-5 w-5 mr-2" />
                Retake Quiz
              </Button>
            </div>
          )}

          {quizProgressState === 'perfected' && quizReviewData && (
            <div className="mt-10 p-6 bg-green-900/70 border border-green-600 rounded-xl shadow-xl text-center">
              <Award className="h-16 w-16 text-yellow-400 mx-auto mb-4" />
              <h2 className="text-2xl font-bold mb-2 text-green-200">Quiz Perfected!</h2>
              <p className="text-xl text-gray-100 mb-1">
                Your score: <span className="font-bold text-yellow-300">100%</span> ({quizReviewData.rawScore}/{quizReviewData.totalQuestions})
              </p>
              <p className="text-md text-gray-300 mb-4">
                You've earned {quizReviewData.experienceEarned || 0} XP!
              </p>
              {isCompleted && (
                <div className="flex items-center justify-center gap-2 text-green-300">
                  <CheckCircle className="h-5 w-5" />
                  <span>Lesson marked as completed!</span>
                </div>
              )}
            </div>
          )}

          {quizSpecificError && (
             <div className="mt-6 p-4 bg-red-900/50 border border-red-700 rounded-md text-red-300 flex items-center gap-3">
               <AlertTriangle className="h-5 w-5 flex-shrink-0" />
               <p>{quizSpecificError}</p>
             </div>
          )}

          <div className={`flex justify-between mt-${(quizDetails || quizReviewData) ? '10' : '10'}`}>
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
            >
              {nextLesson ? (
                <>
                  Next Lesson
                  <ArrowRight className="h-4 w-4 ml-2" />
                </>
              ) : (
                "Back to Course" 
              )}
            </Button>
          </div>
        </div>
      </main>
    </div>
  )
}
