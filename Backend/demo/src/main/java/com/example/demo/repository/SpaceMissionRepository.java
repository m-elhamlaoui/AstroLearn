package com.example.demo.repository;

import com.example.demo.model.SpaceMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SpaceMissionRepository extends JpaRepository<SpaceMission, Long> {
    List<SpaceMission> findByLaunchDateBetween(LocalDateTime start, LocalDateTime end);
    List<SpaceMission> findByStatusOrderByLaunchDateAsc(SpaceMission.MissionStatus status);

    @Query("SELECT m FROM SpaceMission m WHERE m.launchDate BETWEEN :start AND :end ORDER BY m.launchDate")
    List<SpaceMission> findUpcomingMissions(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);
}

