package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.Topic;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Conversion {@link Topic} -> {@link TopicDto}, générée par MapStruct à la compilation.
 */
@Mapper
public interface TopicMapper {

    TopicDto toDto(Topic topic);

    List<TopicDto> toDtoList(List<Topic> topics);
}
