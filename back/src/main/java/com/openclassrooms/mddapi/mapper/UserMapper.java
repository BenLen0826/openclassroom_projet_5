package com.openclassrooms.mddapi.mapper;

import com.openclassrooms.mddapi.dto.UserDto;
import com.openclassrooms.mddapi.entity.User;
import org.mapstruct.Mapper;

/** Conversion {@link User} -> {@link UserDto}, générée par MapStruct à la compilation. */
@Mapper
public interface UserMapper {

    UserDto toDto(User user);
}
