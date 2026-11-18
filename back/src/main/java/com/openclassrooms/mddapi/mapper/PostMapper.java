package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.PostDto;
import com.openclassrooms.mddapi.entity.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** Conversion {@link Post} -> {@link PostDto}, générée par MapStruct à la compilation. */
@Mapper
public interface PostMapper {

    @Mapping(target = "authorUsername", source = "author.username")
    @Mapping(target = "topicId", source = "topic.id")
    @Mapping(target = "topicName", source = "topic.name")
    PostDto toDto(Post post);

    List<PostDto> toDtoList(List<Post> posts);
}
