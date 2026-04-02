package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreDbStorage genreStorage;

    public List<Genre> getAllGenres() {
        return genreStorage.getAllGenres();
    }

    public Genre getGenreById(Long id) {
        return genreStorage.getGenreById(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден."));
    }

    public void validateGenreExists(Long genreId) {
        if (genreId == null) {
            throw new ValidationException("ID жанра не может быть null");
        }
        genreStorage.getGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId + " не найден"));
    }
}