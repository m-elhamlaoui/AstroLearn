package com.example.demo.mapper;

import com.example.demo.dto.CommentDTO;
import com.example.demo.model.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "authorUsername", source = "user.username")
    @Mapping(target = "articleId", source = "article.id")
    CommentDTO toDto(Comment comment);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "article", ignore = true)
    Comment toEntity(CommentDTO commentDTO);
}