package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.TopicDto;
import com.openclassrooms.mddapi.entity.Topic;
import org.mapstruct.Mapper;

/**
 * Conversion {@link Topic} -> {@link TopicDto}, générée par MapStruct à la compilation.
 */
@Mapper
public interface TopicMapper {

    /** {@code subscribed} ne vient pas du thème : il dépend de l'utilisateur connecté. */
    TopicDto toDto(Topic topic, boolean subscribed);
}
