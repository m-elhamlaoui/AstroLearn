package com.example.demo.mapper;

import com.example.demo.model.*;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface BaseMapper {
    @Named("tagsToTagIds")
    default List<Long> tagsToTagIds(List<ArticleTag> tags) {
        if (tags == null)
            return Collections.emptyList();
        return tags.stream().map(ArticleTag::getId).collect(Collectors.toList());
    }

    @Named("modulesToModuleIds")
    default List<Long> modulesToModuleIds(List<com.example.demo.model.Module> modules) {
        if (modules == null)
            return Collections.emptyList();
        return modules.stream().map(com.example.demo.model.Module::getId).collect(Collectors.toList());
    }

    @Named("lessonsToLessonIds")
    default List<Long> lessonsToLessonIds(List<Lesson> lessons) {
        if (lessons == null)
            return Collections.emptyList();
        return lessons.stream().map(Lesson::getId).collect(Collectors.toList());
    }

    @Named("historiesToHistoryIds")
    default List<Long> historiesToHistoryIds(List<ReadingHistory> histories) {
        if (histories == null)
            return Collections.emptyList();
        return histories.stream().map(ReadingHistory::getId).collect(Collectors.toList());
    }

    @Named("progressesToProgressIds")
    default List<Long> progressesToProgressIds(List<CourseProgress> progresses) {
        if (progresses == null)
            return Collections.emptyList();
        return progresses.stream().map(CourseProgress::getId).collect(Collectors.toList());
    }

    @Named("completionsToCompletionIds")
    default List<Long> completionsToCompletionIds(List<QuizCompletion> completions) {
        if (completions == null)
            return Collections.emptyList();
        return completions.stream().map(QuizCompletion::getId).collect(Collectors.toList());
    }

    @Named("missionsToMissionIds")
    default List<Long> missionsToMissionIds(List<SpaceMission> missions) {
        if (missions == null)
            return Collections.emptyList();
        return missions.stream().map(SpaceMission::getId).collect(Collectors.toList());
    }

    @Named("mapTagsToStrings")
    default Set<String> mapTagsToStrings(Set<ArticleTag> tags) {
        if (tags == null) {
            return Set.of();
        }
        return tags.stream()
                .map(ArticleTag::getName)
                .collect(Collectors.toSet());
    }
}