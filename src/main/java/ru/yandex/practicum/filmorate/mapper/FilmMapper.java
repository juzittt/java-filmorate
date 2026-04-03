package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FilmMapper {

    public Film toEntity(NewFilmRequest request) {
        return Film.builder()
                .name(request.getName())
                .description(request.getDescription())
                .releaseDate(request.getReleaseDate())
                .duration(request.getDuration())
                .mpa(toEntity(request.getMpa()))
                .genres(mapGenreDtosToEntities(request.getGenres()))
                .build();
    }

    public FilmDto toDto(Film film) {
        return FilmDto.builder()
                .id(film.getFilmId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(toDto(film.getMpa()))
                .genres(film.getGenres().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public void updateEntityFromRequest(Film film, UpdateFilmRequest request) {
        if (request.getName() != null) film.setName(request.getName());
        if (request.getDescription() != null) film.setDescription(request.getDescription());
        if (request.getReleaseDate() != null) film.setReleaseDate(request.getReleaseDate());
        if (request.getDuration() != null) film.setDuration(request.getDuration());
        if (request.getMpa() != null) film.setMpa(toEntity(request.getMpa()));
        if (request.getGenres() != null) {
            film.setGenres(mapGenreDtosToEntities(request.getGenres()));
        }
    }

    public MpaRating toEntity(MpaDto dto) {
        if (dto == null) return null;
        return MpaRating.builder()
                .ratingId(dto.getId())
                .name(dto.getName())
                .build();
    }

    public MpaDto toDto(MpaRating rating) {
        if (rating == null) return null;
        return MpaDto.builder()
                .id(rating.getRatingId())
                .name(rating.getName())
                .build();
    }

    private List<Genre> mapGenreDtosToEntities(List<GenreDto> genreDtos) {
        if (genreDtos == null || genreDtos.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, GenreDto> uniqueById = genreDtos.stream()
                .filter(dto -> dto != null && dto.getId() != null)
                .collect(Collectors.toMap(
                        GenreDto::getId,
                        dto -> dto,
                        (existing, replacement) -> existing
                ));

        return uniqueById.values().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    public Genre toEntity(GenreDto dto) {
        if (dto == null) return null;
        return Genre.builder()
                .genreId(dto.getId())
                .name(dto.getName())
                .build();
    }

    public GenreDto toDto(Genre genre) {
        if (genre == null) return null;
        return GenreDto.builder()
                .id(genre.getGenreId())
                .name(genre.getName())
                .build();
    }
}