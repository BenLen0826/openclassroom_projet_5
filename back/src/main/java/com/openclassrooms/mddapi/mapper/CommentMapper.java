package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.CommentDto;
import com.openclassrooms.mddapi.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Conversion {@link Comment} -> {@link CommentDto}, générée par MapStruct à la compilation. */
@Mapper
public interface CommentMapper {

    @Mapping(target = "authorUsername", source = "author.username")
    CommentDto toDto(Comment comment);

    List<CommentDto> toDtoList(List<Comment> comments);
}
