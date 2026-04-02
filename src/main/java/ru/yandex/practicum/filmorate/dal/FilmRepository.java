package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY = """
            SELECT
                f.film_id,
                f.title,
                f.description,
                f.release_date,
                f.duration,
                f.rating_id,
                r.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_rating r ON f.rating_id = r.rating_id
            """;
    private static final String FIND_BY_ID_QUERY = """
            SELECT
                f.film_id,
                f.title,
                f.description,
                f.release_date,
                f.duration,
                f.rating_id,
                r.name AS mpa_name
            FROM films f
            LEFT JOIN mpa_rating r ON f.rating_id = r.rating_id
            WHERE f.film_id = ?
            """;
    private static final String INSERT_QUERY = """
            INSERT INTO films (title, description, release_date, duration, rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, rating_id = ?
            WHERE film_id = ?
            """;
    private static final String FIND_MOST_POPULAR_QUERY = """
            SELECT
                f.film_id,
                f.title,
                f.description,
                f.release_date,
                f.duration,
                f.rating_id,
                mr.name AS mpa_name,
                COUNT(l.user_id) AS like_count
            FROM films f
            LEFT JOIN likes l ON f.film_id = l.film_id
            LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
            GROUP BY f.film_id, mr.name
            ORDER BY like_count DESC
            LIMIT ?
            """;
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE_QUERY = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Film save(Film film) {
        if (film.getFilmId() == null) {
            long id = insert(INSERT_QUERY,
                    film.getName(),
                    film.getDescription(),
                    Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa() != null ? film.getMpa().getRatingId() : null
            );
            film.setFilmId(id);
        } else {
            update(UPDATE_QUERY,
                    film.getName(),
                    film.getDescription(),
                    java.sql.Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa() != null ? film.getMpa().getRatingId() : null,
                    film.getFilmId());
        }
        return film;
    }

    public List<Film> findMostPopular(int count) {
        return findMany(FIND_MOST_POPULAR_QUERY, count);
    }

    public Set<Long> findLikes(Long filmId) {
        return jdbc.queryForList(FIND_LIKES_QUERY, Long.class, filmId)
                .stream().collect(Collectors.toSet());
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE_QUERY, filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE_QUERY, filmId, userId);
    }

    public boolean deleteFilm(Long filmId) {
        if (!delete(DELETE_FILM_QUERY, filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        return delete(DELETE_FILM_QUERY, filmId);
    }
}