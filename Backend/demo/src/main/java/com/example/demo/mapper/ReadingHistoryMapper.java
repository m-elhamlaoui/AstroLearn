package com.example.demo.mapper;

import com.example.demo.dto.ReadingHistoryDTO;
import com.example.demo.model.ReadingHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReadingHistoryMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "articleId", source = "article.id")
    ReadingHistoryDTO toDto(ReadingHistory readingHistory);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    ReadingHistory toEntity(ReadingHistoryDTO readingHistoryDTO);
}