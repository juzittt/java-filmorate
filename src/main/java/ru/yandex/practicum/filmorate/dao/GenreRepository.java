package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreRepository {
    List<Genre> findAll();
    Optional<Genre> findById(Long id);
    List<Genre> findGenresByFilmId(Long filmId);
    void addGenresToFilm(Long filmId, List<Genre> genres);
    void replaceGenres(Long filmId, List<Genre> genres);
    void deleteGenresByFilmId(Long filmId);
}