package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String ADD_GENRE = "MERGE INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String FIND_GENRES_BY_FILM_ID = """
            SELECT g.genre_id, g.name
            FROM genres g
            JOIN film_genres fg ON g.genre_id = fg.genre_id
            WHERE fg.film_id = ?
            ORDER BY g.genre_id
            """;
    private static final String DELETE_GENRES_BY_FILM_ID = "DELETE FROM film_genres WHERE film_id = ?";


    public GenreRepository(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public void addGenresToFilm(Long filmId, List<Genre> genres) {
        jdbc.update(DELETE_GENRES_BY_FILM_ID, filmId);

        if (genres == null || genres.isEmpty()) {
            return;
        }

        for (Genre genre : genres) {
            jdbc.update(ADD_GENRE, filmId, genre.getGenreId());
        }
    }

    public List<Genre> findGenresByFilmId(Long filmId) {
        return jdbc.query(FIND_GENRES_BY_FILM_ID, new GenreRowMapper(), filmId);
    }
}