"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group"
import { Label } from "@/components/ui/label"
import { ArrowLeft, ArrowRight, CheckSquare } from "lucide-react"

interface QuizQuestionDTO {
  id: number // Actual ID of the question
  questionText: string
  options: string[]
}

interface QuizDTO {
  id: number
  title: string
  questions: QuizQuestionDTO[] // Questions are part of QuizDTO
  experienceReward: number
}

interface QuizDisplayProps {
  quiz: QuizDTO
  onQuizSubmit: (answers: Map<number, number>) => Promise<void> // questionId to selectedOptionIndex
  isSubmitting: boolean
  initialAnswers?: Map<number, number> // To repopulate answers if retaking
}

export default function QuizDisplay({ quiz, onQuizSubmit, isSubmitting, initialAnswers }: QuizDisplayProps) {
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)
  // Stores questionId -> chosenOptionIndex
  const [selectedAnswers, setSelectedAnswers] = useState<Map<number, number>>(initialAnswers || new Map())

  useEffect(() => {
    // If initialAnswers are provided (e.g., on retake after review), set them.
    if (initialAnswers) {
      setSelectedAnswers(new Map(initialAnswers));
    }
    // Reset current question to the start when the quiz prop changes (e.g. new quiz loaded)
    // or if we want to force reset on retake (though parent can control this by changing key or initialAnswers)
    setCurrentQuestionIndex(0); 
  }, [quiz, initialAnswers]);


  const currentQuestion = quiz.questions[currentQuestionIndex]

  const handleAnswerChange = (questionId: number, optionIndex: number) => {
    const newAnswers = new Map(selectedAnswers)
    newAnswers.set(questionId, optionIndex)
    setSelectedAnswers(newAnswers)
  }

  const handleNext = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1)
    }
  }

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1)
    }
  }

  const handleSubmit = () => {
    // Basic validation: ensure all questions have an attempt if desired,
    // or submit what's available. For now, let's require all.
    if (selectedAnswers.size !== quiz.questions.length) {
      // This alert can be replaced by a more integrated UI message
      alert("Please answer all questions before submitting.")
      return
    }
    onQuizSubmit(selectedAnswers)
  }

  if (!currentQuestion) {
    return <p className="text-red-500">Error: Could not load current question.</p>;
  }

  return (
    <div className="mt-10 p-6 bg-gray-800/70 rounded-xl shadow-xl">
      <div className="flex justify-between items-center mb-2">
        <h2 className="text-2xl font-bold text-yellow-400">{quiz.title}</h2>
        <p className="text-sm text-gray-400">
          Question {currentQuestionIndex + 1} of {quiz.questions.length}
        </p>
      </div>
      <p className="mb-6 text-gray-300">
        Earn up to {quiz.experienceReward} XP!
      </p>
      
      <div key={currentQuestion.id} className="mb-8 p-4 border border-gray-700 rounded-lg bg-gray-900/60">
        <p className="text-xl font-semibold mb-4 text-gray-100">
          {currentQuestion.questionText}
        </p>
        <RadioGroup
          onValueChange={(value) => handleAnswerChange(currentQuestion.id, parseInt(value))}
          value={selectedAnswers.get(currentQuestion.id)?.toString()} // Ensure value is string for RadioGroup
          className="space-y-2"
        >
          {currentQuestion.options.map((option, oIndex) => (
            <div 
              key={oIndex} 
              className="flex items-center space-x-3 p-3 rounded-md hover:bg-gray-700/50 transition-colors cursor-pointer"
              onClick={() => handleAnswerChange(currentQuestion.id, oIndex)} // Allow clicking whole div
            >
              <RadioGroupItem 
                value={oIndex.toString()} 
                id={`q${currentQuestion.id}-o${oIndex}`} 
                className="border-gray-600 text-yellow-400 focus:ring-yellow-500"
              />
              <Label htmlFor={`q${currentQuestion.id}-o${oIndex}`} className="text-gray-200 cursor-pointer flex-1">
                {option}
              </Label>
            </div>
          ))}
        </RadioGroup>
      </div>

      <div className="flex justify-between items-center mt-6">
        <Button
          onClick={handlePrevious}
          disabled={currentQuestionIndex === 0 || isSubmitting}
          variant="outline"
          className="border-gray-600 text-gray-300 hover:bg-gray-700"
        >
          <ArrowLeft className="h-4 w-4 mr-2" />
          Previous
        </Button>

        {currentQuestionIndex < quiz.questions.length - 1 ? (
          <Button
            onClick={handleNext}
            disabled={isSubmitting}
            className="bg-blue-500 hover:bg-blue-600 text-white"
          >
            Next Question
            <ArrowRight className="h-4 w-4 ml-2" />
          </Button>
        ) : (
          <Button
            onClick={handleSubmit}
            disabled={isSubmitting || selectedAnswers.size !== quiz.questions.length}
            className="bg-green-500 hover:bg-green-600 text-white"
          >
            <CheckSquare className="h-4 w-4 mr-2" />
            Submit Answers
          </Button>
        )}
      </div>
    </div>
  )
}
