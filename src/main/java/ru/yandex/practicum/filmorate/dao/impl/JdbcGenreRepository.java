package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.dao.GenreRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcGenreRepository implements GenreRepository {

    private final JdbcTemplate jdbc;

    private static final String FIND_ALL = "SELECT * FROM genres ORDER BY genre_id";
    private static final String FIND_BY_ID = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_BY_FILM_ID = """
        SELECT g.genre_id, g.name
        FROM genres g
        JOIN film_genres fg ON g.genre_id = fg.genre_id
        WHERE fg.film_id = ?
        ORDER BY g.genre_id
        """;
    private static final String ADD_GENRE_TO_FILM = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_GENRES_BY_FILM = "DELETE FROM film_genres WHERE film_id = ?";

    @Override
    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL, new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(Long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID, new GenreRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addGenresToFilm(Long filmId, List<Genre> genres) {
        jdbc.update(DELETE_GENRES_BY_FILM, filmId);
        if (genres != null && !genres.isEmpty()) {
            for (Genre genre : genres) {
                jdbc.update(ADD_GENRE_TO_FILM, filmId, genre.getGenreId());
            }
        }
    }

    @Override
    public List<Genre> findGenresByFilmId(Long filmId) {
        return jdbc.query(FIND_BY_FILM_ID, new GenreRowMapper(), filmId);
    }

    public void replaceGenres(Long filmId, List<Genre> genres) {
        addGenresToFilm(filmId, genres);
    }

    public void deleteGenresByFilmId(Long filmId) {
        jdbc.update(DELETE_GENRES_BY_FILM, filmId);
    }
}