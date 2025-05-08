package com.example.demo.mapper;

import com.example.demo.dto.RecommendationDTO;
import com.example.demo.model.Recommendation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "articleId", source = "article.id")
    RecommendationDTO toDto(Recommendation recommendation);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    Recommendation toEntity(RecommendationDTO recommendationDTO);
}