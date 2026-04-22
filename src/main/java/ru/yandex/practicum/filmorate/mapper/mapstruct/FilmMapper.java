package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.*;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;

@Mapper (
        componentModel = "spring",
        uses = {MpaRatingMapper.class, GenreMapper.class, DirectorMapper.class}
)
public interface FilmMapper {

    @Mapping(target = "filmId", ignore = true)
    Film toEntity(NewFilmRequest request);

    @Mapping(target = "id", source = "filmId")
    FilmDto toDto(Film film);

    @Mapping(target = "filmId", ignore = true)
    void updateFilmFromRequest(UpdateFilmRequest request, @MappingTarget Film film);
}
