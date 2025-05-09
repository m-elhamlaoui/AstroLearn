"use client"

import { useState, useEffect, use } from "react"
import { MinimalNavigation } from "@/components/minimal-navigation"
import QuizDisplay from "@/components/quiz/QuizDisplay" 
import QuizReviewDisplay from "@/components/quiz/QuizReviewDisplay" // Import the new review component
import { Button } from "@/components/ui/button"
import { ArrowLeft, ArrowRight, CheckCircle, Award, AlertTriangle, RotateCcw } from "lucide-react" // Added RotateCcw
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
  quizId: number | null // This will be the ID for the quiz itself
}

interface QuizQuestionDTO {
  id: number
  questionText: string
  options: string[]
  // correctOptionIndex?: number; // Might be needed for answer checking, but not for display from QuizDTO
}

interface QuizDTO {
  id: number
  title: string
  questions: QuizQuestionDTO[]
  lessonId: number
  lessonTitle: string
  experienceReward: number
}

// DTO for submitting quiz answers
interface QuizQuestionAnswerDTO {
  questionId: number
  chosenOptionIndex: number
}

interface QuizSubmissionDTO {
  userId: number
  answers: QuizQuestionAnswerDTO[]
}

// This interface will represent the payload sent in the POST request body
interface QuizSubmissionPayload {
  answers: QuizQuestionAnswerDTO[];
}

// DTO for quiz completion result
// This will be replaced by AugmentedQuizCompletionDTO after backend update
interface QuizCompletionDTO { // Keep for now if parts of code still reference it before full refactor
  id: number;
  score: number; // Raw score (number of correct answers)
  completionDate: string;
  userId: number;
  username: string;
  quizId: number;
  quizTitle: string;
}

// --- NEW DTOs for detailed quiz results from backend ---
interface QuizAttemptDetail {
  questionId: number;
  questionText: string; // Assuming backend provides this for convenience
  options: string[];    // Assuming backend provides this
  chosenOptionIndex: number;
  correctOptionIndex: number;
  isCorrect: boolean; // True if chosenOptionIndex === correctOptionIndex (can be derived frontend too)
}

interface AugmentedQuizCompletionDTO {
  id: number; // Completion ID (for this specific attempt/completion)
  rawScore: number; // Number of correct answers
  totalQuestions: number; // Total questions in the quiz
  completionDate: string; // ISO timestamp
  userId: number;
  username: string;
  quizId: number;
  quizTitle: string;
  experienceEarned: number; // XP earned for this attempt/completion
  attemptDetails: QuizAttemptDetail[]; 
}

// DTO for the result of a quiz submission (from POST /submit)
interface QuizSubmissionResultDTO {
  completionId: number;
  rawScore: number;
  totalQuestions: number;
  isPerfected: boolean;
  experienceEarned: number;
}
// --- End of NEW DTOs ---

type QuizProgressState = 'unstarted' | 'active' | 'reviewing' | 'perfected' | 'show_score_retake';

interface ModuleWithLessonsDTO {
  id: number
  title: string
  lessons: { id: number; title: string }[] // Simplified lesson for navigation structure
}

interface CourseProgressDTO {
  completedLessonIds: number[]
  // currentLessonId might also be part of this if needed globally
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
  const [quizDetails, setQuizDetails] = useState<QuizDTO | null>(null)
  const [quizReviewData, setQuizReviewData] = useState<AugmentedQuizCompletionDTO | null>(null)
  const [currentCompletionId, setCurrentCompletionId] = useState<number | null>(null); // State to store the ID from submit response
  const [quizProgressState, setQuizProgressState] = useState<QuizProgressState>('unstarted')
  const [quizAttemptKey, setQuizAttemptKey] = useState(0) // Used to force re-mount of QuizDisplay
  const [isCompleted, setIsCompleted] = useState(false) // Lesson completion status (overall)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmittingQuiz, setIsSubmittingQuiz] = useState(false) // For the submission process itself
  const [error, setError] = useState<string | null>(null) // General page errors
  const [quizSpecificError, setQuizSpecificError] = useState<string | null>(null) // Errors related to quiz submission/fetching
  const [nextLesson, setNextLesson] = useState<NavLesson | null>(null)
  const [prevLesson, setPrevLesson] = useState<NavLesson | null>(null)

  // TODO: Replace with actual logged-in user ID
  const userIdForProgress = 1 // This should ideally come from auth context

  // useEffect for fetching initial lesson data
  useEffect(() => {
    if (!params.id || !params.moduleId || !params.lessonId) {
      setError("Missing course, module, or lesson ID.")
      setIsLoading(false)
      return
    }

    const fetchLessonAllData = async () => {
      setIsLoading(true)
      setError(null)
      setQuizSpecificError(null) // Reset quiz-specific errors
      setLessonDetails(null)
      setQuizDetails(null)
      setQuizReviewData(null)    // Reset review data
      setQuizProgressState('unstarted') // Reset quiz state

      try {
        // 1. Fetch current lesson details
        const lessonResponse = await axiosInstance.get<LessonDTO>(`/lessons/${params.lessonId}`)
        const currentLesson = lessonResponse.data
        setLessonDetails(currentLesson)

        // 2. Fetch course progress
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

        // 3. Fetch Quiz if quizId exists on the lesson
        if (currentLesson && currentLesson.quizId) {
          try {
            const quizResponse = await axiosInstance.get<QuizDTO>(`/quizzes/lessons/${currentLesson.id}`)
            setQuizDetails(quizResponse.data)

            // If lesson is marked completed in the system, it implies the quiz (if any) was perfected.
            // Fetch the detailed completion data for potential review or just to confirm.
            if (lessonAlreadyMarkedCompletedInSystem && currentLesson.quizId) {
              try {
                // Assuming GET /quizzes/{quizId}/completion now returns AugmentedQuizCompletionDTO
                // or a similar detailed DTO if the user has a completion record.
                const completionResponse = await axiosInstance.get<AugmentedQuizCompletionDTO>(
                  `/quizzes/${currentLesson.quizId}/completion?userId=${userIdForProgress}`
                )
                if (completionResponse.data && completionResponse.data.rawScore === completionResponse.data.totalQuestions) {
                  setQuizReviewData(completionResponse.data)
                  setQuizProgressState('perfected') 
                } else {
                  // Lesson marked complete, but quiz data suggests not perfected or no data.
                  // Default to 'unstarted' to allow taking it if desired.
                  setQuizProgressState('unstarted')
                }
              } catch (completionFetchError) {
                console.warn("Failed to fetch prior detailed quiz completion data:", completionFetchError)
                setQuizProgressState('unstarted') // Default to unstarted if fetch fails
              }
            } else if (currentLesson.quizId) {
              // Lesson not marked as completed in system, so quiz is 'unstarted'
              setQuizProgressState('unstarted')
            }
            // If no quizId, quizProgressState remains 'unstarted' but won't trigger quiz UI
          } catch (quizFetchError) {
            console.error("Failed to load quiz details:", quizFetchError)
            setQuizSpecificError("Could not load quiz details.")
          }
        } else if (currentLesson && !currentLesson.quizId && !lessonAlreadyMarkedCompletedInSystem) {
          // If no quiz and lesson not completed, mark as complete
          try {
            await axiosInstance.post(
              `/course-progress/${userIdForProgress}/${params.id}/lessons/${params.lessonId}/complete`,
            )
            setIsCompleted(true)
          } catch (completionError) {
            console.warn("Failed to mark lesson (no quiz) as complete:", completionError)
          }
        }
        
        // 4. Fetch full course structure for navigation
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

  // useEffect to fetch review data when currentCompletionId is set and in 'reviewing' state
  useEffect(() => {
    const fetchReviewData = async () => {
      if (!currentCompletionId || quizProgressState !== 'reviewing' || !lessonDetails) return;
      // Avoid refetching if reviewData is already present for this completionId (unless forced)
      if (quizReviewData && quizReviewData.id === currentCompletionId) return;

      // setIsLoading(true); // Consider a more specific loading state for review data if needed
      setQuizSpecificError(null);
      try {
        const reviewResponse = await axiosInstance.get<AugmentedQuizCompletionDTO>(
          `/quizzes/completions/${currentCompletionId}` // Use the new endpoint
        );
        setQuizReviewData(reviewResponse.data);

        if (reviewResponse.data.rawScore === reviewResponse.data.totalQuestions) {
          setQuizProgressState('perfected'); // Update state if perfected
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
      } finally {
        // setIsLoading(false); // Stop loading indicator for review data
      }
    };

    fetchReviewData();
  }, [currentCompletionId, quizProgressState, lessonDetails, userIdForProgress, params.id, isCompleted, quizReviewData]); // quizReviewData added to prevent re-fetch if already loaded

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

    // Constructing the payload without userId in the body, as it's a RequestParam
    const submissionPayload: QuizSubmissionPayload = {
      answers: submissionAnswers,
    }

    try {
      const response = await axiosInstance.post<QuizSubmissionResultDTO>(
        `/quizzes/${quizDetails.id}/submit?userId=${userIdForProgress}`,
        submissionPayload,
      );
      const result = response.data;
      setCurrentCompletionId(result.completionId); // Store completion ID

      if (result.isPerfected) {
        setQuizProgressState('perfected');
        // Construct a minimal AugmentedQuizCompletionDTO for the 'perfected' screen display
        setQuizReviewData({ 
            id: result.completionId,
            rawScore: result.rawScore,
            totalQuestions: result.totalQuestions,
            completionDate: new Date().toISOString(), // Current time as placeholder
            userId: userIdForProgress,
            username: "User", // Placeholder, or fetch if needed
            quizId: quizDetails.id,
            quizTitle: quizDetails.title,
            experienceEarned: result.experienceEarned,
            attemptDetails: [] // No detailed attempts shown on initial "perfected" screen
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
        // Not perfected, show score and retake option
        setQuizProgressState('show_score_retake');
        // Store score info in quizReviewData for the 'show_score_retake' state
        setQuizReviewData({ 
            id: result.completionId,
            rawScore: result.rawScore,
            totalQuestions: result.totalQuestions,
            completionDate: new Date().toISOString(),
            userId: userIdForProgress,
            username: "User", // Placeholder
            quizId: quizDetails.id,
            quizTitle: quizDetails.title,
            experienceEarned: result.experienceEarned, // Might be 0 if not perfected
            attemptDetails: [] // No details shown here
        });
      }
    } catch (err: any) {
      console.error("Quiz submission failed:", err);
      // Log the full error for debugging backend issues
      if (err.response) {
        console.error("Backend Error Response:", err.response.data);
        console.error("Backend Error Status:", err.response.status);
        console.error("Backend Error Headers:", err.response.headers);
      } else if (err.request) {
        console.error("Backend No Response:", err.request);
      } else {
        console.error("Axios Setup Error:", err.message);
      }
      const errorMessage = err.response?.data?.message || "Failed to submit quiz. Please try again."
      setQuizSpecificError(errorMessage)
      // Potentially set quizProgressState to 'unstarted' or show error prominently
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
              key={quizAttemptKey} // Force re-mount on new attempt
              quiz={quizDetails}
              onQuizSubmit={handleQuizSubmit}
              isSubmitting={isSubmittingQuiz}
              initialAnswers={new Map()} // Ensure fresh start for answers
            />
          )}

          {/* Reviewing state now needs to fetch data */}
          {quizProgressState === 'reviewing' && (
            <>
              {/* Placeholder/Loading state while fetching review data */}
              {!quizReviewData && currentCompletionId && (
                <div className="mt-10 text-center p-6 bg-gray-800 rounded-xl">
                  <p className="text-lg mb-4">Loading your results...</p>
                  {/* Loading indicator could be more sophisticated, e.g. a spinner */}
                </div>
              )}
              {/* Display review data once fetched */}
              {quizReviewData && (
                 <>
                   <QuizReviewDisplay reviewData={quizReviewData} />
                   <div className="mt-6 text-center">
                     {/* Show Retake button only if not perfected */}
                     {quizReviewData.rawScore !== quizReviewData.totalQuestions && (
                       <Button
                         onClick={() => {
                           setQuizProgressState('active');
                           setQuizAttemptKey(prevKey => prevKey + 1); // Increment key to force re-mount
                           setQuizSpecificError(null); // Clear previous submission errors
                           setCurrentCompletionId(null); // Clear old completion ID
                           setQuizReviewData(null); // Clear review data
                         }}
                         className="bg-blue-500 hover:bg-blue-600 text-white font-semibold py-3 px-6 text-lg"
                       >
                         <RotateCcw className="h-5 w-5 mr-2" />
                         Retake Quiz
                       </Button>
                     )}
                     {/* If perfected, maybe show a different message or just the navigation */}
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

          {/* State to show score and retake button after a non-perfect attempt */}
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
              {/* "Review Answers" button removed from perfected state as per request */}
            </div>
          )}

          {quizSpecificError && (
             <div className="mt-6 p-4 bg-red-900/50 border border-red-700 rounded-md text-red-300 flex items-center gap-3">
               <AlertTriangle className="h-5 w-5 flex-shrink-0" />
               <p>{quizSpecificError}</p>
             </div>
          )}

          {/* Navigation Buttons */}
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
