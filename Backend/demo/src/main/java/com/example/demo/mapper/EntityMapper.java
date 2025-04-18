package com.example.demo.mapper; // Adjust package if needed

import com.example.demo.dto.*; // Import all your DTOs
import com.example.demo.model.*; // Import all your Entities
import org.mapstruct.*; // Import MapStruct annotations

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, // Optional: Ignore null DTO fields during updates
        uses = { EntityMapper.class } // Allows the mapper to call itself for nested objects like QuizQuestion
)
public interface EntityMapper {

    // ==================== Article Mappings ====================

    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "tagsToTagNames")
        // averageRating and commentCount are mapped implicitly via @Formula fields
    ArticleDTO toDTO(Article article);

    @Mapping(target = "author", ignore = true) // Handled by service
    @Mapping(target = "comments", ignore = true) // Handled by service/cascade
    @Mapping(target = "ratings", ignore = true) // Handled by service/cascade
    @Mapping(target = "tags", ignore = true) // Handled by service
    @Mapping(target = "averageRating", ignore = true) // Read-only calculated field
    @Mapping(target = "commentCount", ignore = true) // Read-only calculated field
    Article toEntity(ArticleDTO articleDTO);

    // Helper for Tags
    @Named("tagsToTagNames")
    default Set<String> tagsToTagNames(Set<ArticleTag> tags) {
        if (tags == null) {
            return Collections.emptySet();
        }
        return tags.stream().map(ArticleTag::getName).collect(Collectors.toSet());
    }

    // ==================== ArticleRating Mappings ====================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "articleId", source = "article.id")
    ArticleRatingDTO toDTO(ArticleRating articleRating);

    @Mapping(target = "user", ignore = true) // Handled by service
    @Mapping(target = "article", ignore = true) // Handled by service
    ArticleRating toEntity(ArticleRatingDTO articleRatingDTO);

    // ==================== ArticleTag Mappings ====================

    ArticleTagDTO toDTO(ArticleTag articleTag);

    @Mapping(target = "articles", ignore = true) // Avoid mapping back-reference
    ArticleTag toEntity(ArticleTagDTO articleTagDTO);


    // ==================== Comment Mappings ====================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "authorUsername", source = "user.username")
    @Mapping(target = "articleId", source = "article.id")
    CommentDTO toDTO(Comment comment);

    @Mapping(target = "user", ignore = true) // Handled by service
    @Mapping(target = "article", ignore = true) // Handled by service
    Comment toEntity(CommentDTO commentDTO);


    // ==================== Course Mappings ====================

    @Mapping(target = "moduleIds", source = "modules", qualifiedByName = "modulesToModuleIds")
    @Mapping(target = "totalLessons", expression = "java(course.getTotalLessons())") // Use @Transient method
    CourseDTO toDTO(Course course);

    @Mapping(target = "modules", ignore = true) // Handled by service/cascade
    @Mapping(target = "progresses", ignore = true) // Managed separately
    @Mapping(target = "totalLessons", ignore = true) // Read-only calculated field
    Course toEntity(CourseDTO courseDTO);


    // ==================== CourseProgress Mappings ====================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "currentLessonId", source = "currentLesson.id") // Handles null currentLesson
    CourseProgressDTO toDTO(CourseProgress courseProgress);

    @Mapping(target = "user", ignore = true) // Handled by service
    @Mapping(target = "course", ignore = true) // Handled by service
    @Mapping(target = "currentLesson", ignore = true) // Handled by service
        // completionPercentage and completed are often calculated fields, map if needed from DTO
    CourseProgress toEntity(CourseProgressDTO courseProgressDTO);


    // ==================== Module Mappings ====================

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "lessonIds", source = "lessons", qualifiedByName = "lessonsToLessonIds")
        // lessonCount is mapped implicitly via @Formula field
    ModuleDTO toDTO(com.example.demo.model.Module module);

    @Mapping(target = "course", ignore = true) // Handled by service
    @Mapping(target = "lessons", ignore = true) // Handled by service/cascade
    @Mapping(target = "lessonCount", ignore = true) // Read-only calculated field
    com.example.demo.model.Module toEntity(ModuleDTO moduleDTO);

    // ==================== Lesson Mappings ====================

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "quizId", source = "quiz.id") // Handles null quiz
    LessonDTO toDTO(Lesson lesson);

    @Mapping(target = "module", ignore = true) // Handled by service
    @Mapping(target = "quiz", ignore = true) // Handled by service/cascade
    Lesson toEntity(LessonDTO lessonDTO);


    // ==================== Quiz Mappings ====================

    @Mapping(target = "lessonId", source = "lesson.id")
    @Mapping(target = "lessonTitle", source = "lesson.title")
    @Mapping(target = "questions", source = "questions") // Requires QuizQuestion mapping
    QuizDTO toDTO(Quiz quiz);

    @Mapping(target = "lesson", ignore = true) // Handled by service
    @Mapping(target = "questions", ignore = true) // Handled by service/cascade
    Quiz toEntity(QuizDTO quizDTO);


    // ==================== QuizCompletion Mappings ====================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "quizId", source = "quiz.id")
    @Mapping(target = "quizTitle", source = "quiz.title")
    QuizCompletionDTO toDTO(QuizCompletion quizCompletion);

    @Mapping(target = "user", ignore = true) // Handled by service
    @Mapping(target = "quiz", ignore = true) // Handled by service
    QuizCompletion toEntity(QuizCompletionDTO quizCompletionDTO);


    // ==================== QuizQuestion Mappings ====================

    // Note: quizId might not be needed in DTO if always nested, but included per your DTO def
    // @Mapping(target = "quizId", source = "quiz.id") // Uncomment if QuizQuestionDTO needs quizId
   // @Mapping(target = "correctOptionIndex", ignore = true) // IMPORTANT: Do not expose correct answer
    QuizQuestionDTO toDTO(QuizQuestion quizQuestion);

    // @Mapping(target = "quiz", ignore = true) // Assuming QuizQuestion is part of Quiz creation/update
    QuizQuestion toEntity(QuizQuestionDTO quizQuestionDTO);


    // ==================== ReadingHistory Mappings ====================

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "articleId", source = "article.id")
    @Mapping(target = "articleTitle", source = "article.title")
    @Mapping(target = "isRead", expression = "java(readingHistory.getTimeSpentSeconds() > 0)") // Calculate isRead from timeSpentSeconds
    ReadingHistoryDTO toDTO(ReadingHistory readingHistory);

    @Mapping(target = "user", ignore = true) // Handled by service
    @Mapping(target = "article", ignore = true) // Handled by service
        // isRead is calculated or set based on timeSpentSeconds, map if needed from DTO
    ReadingHistory toEntity(ReadingHistoryDTO readingHistoryDTO);


    // ==================== SpaceMission Mappings ====================

    @Mapping(target = "creatorUserId", source = "user.id")
    @Mapping(target = "creatorUsername", source = "user.username")
    SpaceMissionDTO toDTO(SpaceMission spaceMission);

    @Mapping(target = "user", ignore = true) // Handled by service
    SpaceMission toEntity(SpaceMissionDTO spaceMissionDTO);


    // ==================== User Mappings ====================
    // Note: Collections map to IDs. Add more ID lists to DTO if needed (e.g., articleIds, commentIds)
    // articleCount, commentCount, quizCompletionCount mapped implicitly by @Formula
    @Mapping(target = "readingHistoryIds", source="readingHistory", qualifiedByName = "historiesToHistoryIds")
    @Mapping(target = "courseProgressIds", source="courseProgress", qualifiedByName = "progressesToProgressIds")
    @Mapping(target = "quizCompletionIds", source="quizCompletions", qualifiedByName = "completionsToCompletionIds")
    @Mapping(target = "createdSpaceMissionIds", source="spaceMissions", qualifiedByName = "missionsToMissionIds")
    UserDTO toDTO(User user);


    @Mapping(target = "password", ignore = true) // Handle password hashing/updates in service
    @Mapping(target = "articles", ignore = true) // Handled by service/cascade
    @Mapping(target = "courseProgress", ignore = true) // Handled by service/cascade
    @Mapping(target = "ratings", ignore = true) // Handled by service/cascade
    @Mapping(target = "comments", ignore = true) // Handled by service/cascade
    @Mapping(target = "readingHistory", ignore = true) // Handled by service/cascade
    @Mapping(target = "quizCompletions", ignore = true) // Handled by service/cascade
    @Mapping(target = "spaceMissions", ignore = true) // Handled by service/cascade
    @Mapping(target = "experiencePoints", ignore = true) // Updated via addExperience method
    @Mapping(target = "level", ignore = true) // Updated via addExperience method
    @Mapping(target = "articleCount", ignore = true) // Read-only calculated field
    @Mapping(target = "commentCount", ignore = true) // Read-only calculated field
    @Mapping(target = "quizCompletionCount", ignore = true) // Read-only calculated field
    User toEntity(UserDTO userDTO);


    // --- Helper Methods for ID Extraction ---

    @Named("modulesToModuleIds")
    default List<Long> modulesToModuleIds(List<com.example.demo.model.Module> modules) {
        if (modules == null) return Collections.emptyList();
        return modules.stream().map(com.example.demo.model.Module::getId).collect(Collectors.toList());
    }

    @Named("lessonsToLessonIds")
    default List<Long> lessonsToLessonIds(List<Lesson> lessons) {
        if (lessons == null) return Collections.emptyList();
        return lessons.stream().map(Lesson::getId).collect(Collectors.toList());
    }

    @Named("historiesToHistoryIds")
    default List<Long> historiesToHistoryIds(List<ReadingHistory> histories) {
        if (histories == null) return Collections.emptyList();
        return histories.stream().map(ReadingHistory::getId).collect(Collectors.toList());
    }

    @Named("progressesToProgressIds")
    default List<Long> progressesToProgressIds(List<CourseProgress> progresses) {
        if (progresses == null) return Collections.emptyList();
        return progresses.stream().map(CourseProgress::getId).collect(Collectors.toList());
    }

    @Named("completionsToCompletionIds")
    default List<Long> completionsToCompletionIds(List<QuizCompletion> completions) {
        if (completions == null) return Collections.emptyList();
        return completions.stream().map(QuizCompletion::getId).collect(Collectors.toList());
    }

    @Named("missionsToMissionIds")
    default List<Long> missionsToMissionIds(List<SpaceMission> missions) {
        if (missions == null) return Collections.emptyList();
        return missions.stream().map(SpaceMission::getId).collect(Collectors.toList());
    }

    // --- Update Methods (Optional but useful for PATCH operations) ---
    // These methods update an existing entity from a DTO, ignoring nulls in the DTO

    @Mapping(target = "id", ignore = true) // Never update ID
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true) // Typically shouldn't be updated
    void updateArticleFromDto(ArticleDTO dto, @MappingTarget Article entity);

    // Add similar update methods for other entities as needed...
    // Example for User:
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "articles", ignore = true)
    @Mapping(target = "courseProgress", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "readingHistory", ignore = true)
    @Mapping(target = "quizCompletions", ignore = true)
    @Mapping(target = "spaceMissions", ignore = true)
    @Mapping(target = "experiencePoints", ignore = true)
    @Mapping(target = "level", ignore = true)
    @Mapping(target = "articleCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "quizCompletionCount", ignore = true)
    @Mapping(target = "role", ignore = true) // Usually role changes require specific logic/permissions
    @Mapping(target = "verificationStatus", ignore = true) // Usually updated by admin actions
    @Mapping(target = "email", ignore = true) // Often requires verification if changed
    void updateUserFromDto(UserDTO dto, @MappingTarget User entity);
}