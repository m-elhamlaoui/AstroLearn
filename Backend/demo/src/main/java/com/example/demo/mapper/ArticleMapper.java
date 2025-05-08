package com.example.demo.mapper;

import com.example.demo.dto.ArticleDTO;
import com.example.demo.model.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ArticleMapper extends BaseMapper {
    @Mapping(target = "authorId", source = "author.id")
    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "mapTagsToStrings")
    ArticleDTO toDto(Article article);

    @Mapping(target = "author", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "votes", ignore = true)
    Article toEntity(ArticleDTO articleDTO);
}