package com.example.demo.repository;

import com.example.demo.model.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface CourseProgressRepository extends JpaRepository<CourseProgress, Long> {

    Optional<CourseProgress> findByUserIdAndCourseId(Long userId, Long courseId);
    List<CourseProgress> findByUserId(Long userId);
    List<CourseProgress> findByCompletedTrue();
    List<CourseProgress> findByCourseIdAndCompletedTrue(Long courseId);

    @Query("SELECT cp FROM CourseProgress cp WHERE cp.user.id = :userId AND cp.completionPercentage >= 80")
    List<CourseProgress> findNearlyCompletedCourses(@Param("userId") Long userId);


    @Modifying
    @Query("UPDATE CourseProgress cp SET cp.currentLesson.id = :lessonId WHERE cp.id = :progressId")
    void updateCurrentLesson(@Param("progressId") Long progressId, @Param("lessonId") Long lessonId);

}

