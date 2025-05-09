"use client"

import { CheckCircle, XCircle, Target } from "lucide-react"

// Assuming these DTOs are defined in a shared types file or passed appropriately
// For standalone component, defining them here for clarity.
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
  quizTitle: string;
  experienceEarned: number; 
  attemptDetails: QuizAttemptDetail[]; 
}

interface QuizReviewDisplayProps {
  reviewData: AugmentedQuizCompletionDTO;
}

export default function QuizReviewDisplay({ reviewData }: QuizReviewDisplayProps) {
  const percentageScore = reviewData.totalQuestions > 0 
    ? ((reviewData.rawScore / reviewData.totalQuestions) * 100)
    : 0;

  return (
    <div className="mt-10 p-6 bg-gray-800/90 rounded-xl shadow-2xl">
      <h2 className="text-3xl font-bold mb-3 text-center text-yellow-400">
        Quiz Review: {reviewData.quizTitle}
      </h2>
      <div className="text-center mb-6 p-4 bg-gray-700/50 rounded-lg">
        <p className="text-2xl font-semibold text-white">
          Your Score: {percentageScore.toFixed(0)}% 
          <span className="text-lg text-gray-300 ml-2">
            ({reviewData.rawScore} / {reviewData.totalQuestions} correct)
          </span>
        </p>
        {reviewData.experienceEarned > 0 && (
            <p className="text-md text-green-400 mt-1">You earned {reviewData.experienceEarned} XP!</p>
        )}
      </div>

      <div className="space-y-6">
        {reviewData.attemptDetails.map((detail, index) => (
          <div key={detail.questionId} className="p-4 border border-gray-700 rounded-lg bg-gray-900/70 shadow-md">
            <p className="text-lg font-semibold mb-3 text-gray-100">
              Question {index + 1}: {detail.questionText}
            </p>
            <ul className="space-y-2">
              {detail.options.map((option, oIndex) => {
                const isChosen = oIndex === detail.chosenOptionIndex;
                const isCorrect = oIndex === detail.correctOptionIndex;
                let itemClass = "p-3 rounded-md transition-all duration-200 ease-in-out text-gray-300 border border-gray-600";
                let icon = null;

                if (isCorrect) {
                  itemClass += " bg-green-800/50 border-green-500 text-green-200";
                  icon = <CheckCircle className="h-5 w-5 text-green-400 mr-2 flex-shrink-0" />;
                } else if (isChosen && !isCorrect) {
                  itemClass += " bg-red-800/50 border-red-500 text-red-200";
                  icon = <XCircle className="h-5 w-5 text-red-400 mr-2 flex-shrink-0" />;
                } else {
                   itemClass += " hover:bg-gray-700/60";
                }
                
                return (
                  <li key={oIndex} className={`flex items-center ${itemClass}`}>
                    {icon}
                    <span>{option}</span>
                    {isChosen && !isCorrect && (
                        <span className="ml-auto text-xs px-2 py-1 bg-red-700 rounded-full">Your Answer</span>
                    )}
                     {isCorrect && !isChosen && (
                        <span className="ml-auto text-xs px-2 py-1 bg-green-700 rounded-full">Correct Answer</span>
                    )}
                     {isCorrect && isChosen && (
                        <span className="ml-auto text-xs px-2 py-1 bg-green-700 rounded-full">Correct!</span>
                    )}
                  </li>
                );
              })}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
