package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

@Mapper(componentModel = "spring")
public interface GenreMapper {

    @Mapping(target = "genreId", source = "id")
    Genre toEntity(GenreDto dto);

    @InheritInverseConfiguration
    GenreDto toDto(Genre genre);
}
