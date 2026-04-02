package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GenreDbStorage {
    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public Optional<Genre> getGenreById(Long id) {
        return genreRepository.findById(id);
    }

    public void addGenresToFilm(Long filmId, List<Genre> genres) {
        genreRepository.addGenresToFilm(filmId, genres);
    }

    public List<Genre> getGenresByFilmId(Long filmId) {
        return genreRepository.findGenresByFilmId(filmId);
    }
}