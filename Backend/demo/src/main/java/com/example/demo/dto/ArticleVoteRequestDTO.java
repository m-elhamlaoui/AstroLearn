package com.example.demo.dto;


import com.example.demo.model.VoteType;
import jakarta.validation.constraints.NotNull;

public record ArticleVoteRequestDTO(
        @NotNull VoteType voteType // User specifies UP or DOWN
) {}