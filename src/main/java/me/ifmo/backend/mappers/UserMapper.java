package me.ifmo.backend.mappers;

import me.ifmo.backend.DTO.UserDTO;
import me.ifmo.backend.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().name())")
    UserDTO toDto(User user);
}
