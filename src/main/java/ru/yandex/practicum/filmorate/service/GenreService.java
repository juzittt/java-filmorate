package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreStorage;
    private final FilmMapper filmMapper;

    public List<GenreDto> getAllGenres() {
        return genreStorage.getAllGenres().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public GenreDto getGenreById(Long id) {
        return genreStorage.getGenreById(id)
                .map(filmMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден."));
    }
}