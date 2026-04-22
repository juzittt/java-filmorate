package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Mapper(componentModel = "spring")
public interface DirectorMapper {

    Director toEntity(DirectorDto dto);

    DirectorDto toDto(Director director);

    void updateDirectorFromDto(@MappingTarget Director director, DirectorDto dto);
}
