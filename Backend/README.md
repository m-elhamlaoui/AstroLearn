# Backend API Changes for Enhanced Quiz Functionality

To enable the new quiz features on the frontend, the following backend modifications are required:

## 1. Update the Response for `POST /quizzes/{quizId}/submit` Endpoint:

When a user submits their answers, this endpoint should now return a JSON object structured like the `AugmentedQuizCompletionDTO`.

### `AugmentedQuizCompletionDTO` Structure:

This DTO represents the overall result of a quiz submission, including detailed feedback.

```java
// Represents details for a single question in an attempt
public record QuizAttemptDetailDTO(
    Long questionId,
    String questionText,     // The full text of the question
    List<String> options,    // The list of options presented to the user
    int chosenOptionIndex,   // The 0-based index of the option the user selected
    int correctOptionIndex,  // The 0-based index of the correct option
    boolean isCorrect        // Convenience field: true if chosenOptionIndex == correctOptionIndex
) {}

// Represents the overall result of a quiz submission, including detailed feedback
public record AugmentedQuizCompletionDTO(
    Long id,                 // Unique ID for this specific quiz completion/attempt
    int rawScore,            // The number of questions the user answered correctly
    int totalQuestions,      // The total number of questions in this quiz
    LocalDateTime completionDate, // Timestamp of when the quiz was completed
    Long userId,
    String username,         // Username of the user who took the quiz
    Long quizId,
    String quizTitle,
    int experienceEarned,    // Experience points awarded for this attempt
    List<QuizAttemptDetailDTO> attemptDetails // List of details for each question
) {}
```

### Key fields to calculate and include in `AugmentedQuizCompletionDTO`:

-   `rawScore`: An integer representing the **number of correctly answered questions**.
-   `totalQuestions`: An integer for the total number of questions in the quiz.
-   `attemptDetails`: This is a crucial list of `QuizAttemptDetailDTO` objects. Each object in the list corresponds to a question from the quiz and must contain:
    -   `questionId`: The ID of the question.
    -   `questionText`: The full text of the question (for easy display on the review screen).
    -   `options`: The list of options presented for that question (for easy display).
    -   `chosenOptionIndex`: The 0-based index of the option selected by the user.
    -   `correctOptionIndex`: The 0-based index of the correct option for that question.
    -   `isCorrect`: A boolean indicating if the user's `chosenOptionIndex` matches the `correctOptionIndex`.

Other fields like `id`, `completionDate`, `userId`, `username`, `quizId`, `quizTitle`, and `experienceEarned` should be populated as standard.

## 2. Update the Response for `GET /quizzes/{quizId}/completion?userId={userId}` Endpoint (Optional but Recommended):

If a user has previously completed a quiz (especially if they perfected it), and they revisit the lesson, the frontend attempts to fetch this past completion record.

-   Ideally, this endpoint should also return the same `AugmentedQuizCompletionDTO` structure if a completion record exists. This would allow users to review their past perfected attempts with the same level of detail.
-   If this endpoint currently returns a simpler `QuizCompletionDTO`, the review feature for *past* attempts won't be as detailed. The primary and most critical need for `AugmentedQuizCompletionDTO` is from the `POST /quizzes/{quizId}/submit` endpoint for immediate review after an attempt.

## Summary for the Backend Team:

-   **Modify `POST /quizzes/{quizId}/submit`:**
    -   It **must** return the `AugmentedQuizCompletionDTO` structure detailed above.
    -   Ensure `rawScore` is the count of correct answers.
    -   Ensure `totalQuestions` is provided.
    -   The `attemptDetails` list is crucial for the review screen. Each item in `attemptDetails` needs to map a question to the user's choice and the correct choice.

-   **Consider Modifying `GET /quizzes/{quizId}/completion?userId={userId}`:**
    -   For consistency and to allow review of previously perfected quizzes, have this endpoint also return `AugmentedQuizCompletionDTO`.

These changes will enable the frontend to:
- Calculate and display the percentage score correctly.
- Provide a detailed review screen where users can see each question, their chosen answer, and the correct answer, with clear visual indicators.
- Support the full quiz lifecycle including retakes and review of perfected quizzes.
