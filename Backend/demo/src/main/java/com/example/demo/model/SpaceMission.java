package com.example.demo.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;


@Table(name = "missions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SpaceMission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String agency;

    private LocalDateTime launchDate;

    @Lob
    private String description;

    private String missionImage;
    private String liveStreamUrl;

    @Enumerated(EnumType.STRING)
    private MissionStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public enum MissionStatus {
        UPCOMING, IN_PROGRESS, COMPLETED, FAILED
    }}

