package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    User toEntity(NewUserRequest request);

    @Mapping(target = "id", source = "userId")
    UserDto toDto(User user);

    @Mapping(target = "userId", ignore = true)
    void updateUserFromRequest(@MappingTarget User user, UpdateUserRequest request);
}