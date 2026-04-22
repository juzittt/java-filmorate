package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.FilmRepository;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JdbcFilmRepository implements FilmRepository {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final JdbcGenreRepository genreRepository;


    private static final String FIND_ALL = """
        SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
               f.rating_id, mr.name AS mpa_name
        FROM films f
        LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
        """;

    private static final String FIND_BY_ID = FIND_ALL + " WHERE f.film_id = ?";
    private static final String INSERT = "INSERT INTO films (title, description, release_date, duration, rating_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET title = ?, description = ?, release_date = ?, duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String DELETE = "DELETE FROM films WHERE film_id = ?";
    private static final String POPULAR_WITH_FILTERS = """
        SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
               f.rating_id, mr.name AS mpa_name, COUNT(l.user_id) AS like_count
        FROM films f
        LEFT JOIN likes l ON f.film_id = l.film_id
        LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
        LEFT JOIN film_genres fg ON f.film_id = fg.film_id
        WHERE (1=1)
          AND (? IS NULL OR fg.genre_id = ?)
          AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
        GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, mr.name
        ORDER BY like_count DESC
        LIMIT ?
        """;
    private static final String FIND_LIKES = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ADD_LIKE = "MERGE INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String REMOVE_LIKE = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_QUERY = "SELECT user_id FROM likes WHERE film_id = ?";

    private static final String INSERT_FILM_DIRECTOR_SQL = """
        INSERT INTO film_directors (film_id, director_id)
        VALUES (?, ?)
        """;

    private static final String DELETE_FILM_DIRECTORS = """
        DELETE FROM film_directors
        WHERE film_id = ?
        """;

    private static final String FIND_DIRECTORS_BY_FILM_ID = """
        SELECT d.director_id, d.name
        FROM film_directors fd
        JOIN directors d ON fd.director_id = d.director_id
        WHERE fd.film_id = ?
        ORDER BY d.director_id
        """;

    private static final String FIND_FILMS_BY_DIRECTOR_ORDER_BY_YEAR = FIND_ALL +
    """
    JOIN film_directors fd ON f.film_id = fd.film_id
    WHERE fd.director_id = ?
    ORDER BY f.release_date ASC, f.film_id ASC
    """;

    private static final String FIND_FILMS_BY_DIRECTOR_ORDER_BY_LIKES = FIND_ALL + """
        JOIN film_directors fd ON f.film_id = fd.film_id
        LEFT JOIN likes l ON f.film_id = l.film_id
        WHERE fd.director_id = ?
        GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, mr.name
        ORDER BY COUNT(l.user_id) DESC, f.film_id ASC
        """;

    private static final String FIND_RECOMMENDATIONS = """
        SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
               f.rating_id, mr.name AS mpa_name
        FROM films f
        LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
        WHERE f.film_id IN (
            SELECT l.film_id
            FROM likes l
            WHERE l.user_id = (
                SELECT l2.user_id
                FROM likes l1
                JOIN likes l2 ON l1.film_id = l2.film_id
                WHERE l1.user_id = ? AND l2.user_id != ?
                GROUP BY l2.user_id
                ORDER BY COUNT(*) DESC
                LIMIT 1
            )
            AND l.film_id NOT IN (
                SELECT film_id FROM likes WHERE user_id = ?
            )
        )
        """;

    private static final String GET_COMMON_FILMS = """
        SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
           r.rating_id, r.name AS mpa_name
        FROM films f
        JOIN mpa_rating r ON f.rating_id = r.rating_id
        JOIN likes l1 ON f.film_id = l1.film_id
        JOIN likes l2 ON f.film_id = l2.film_id
        WHERE l1.user_id = ? AND l2.user_id = ?
        ORDER BY (
            SELECT COUNT(*) FROM likes l WHERE l.film_id = f.film_id
        ) DESC
        """;

    private static final String SEARCH_FILMS = """
        SELECT f.*, mr.name AS mpa_name,
               (SELECT COUNT(*) FROM likes WHERE film_id = f.film_id) AS like_count
        FROM films f
        LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
        LEFT JOIN film_directors fd ON f.film_id = fd.film_id
        LEFT JOIN directors d ON fd.director_id = d.director_id
        WHERE %s
        ORDER BY like_count DESC
        """;

    @Override
    public List<Film> findAll() {
        List<Film> films = jdbc.query(FIND_ALL, filmRowMapper);
        loadFilmsData(films);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID, filmRowMapper, id);
            if (film != null) {
                loadFilmData(film);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film save(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getRatingId() : null
        );
        film.setFilmId(id);
        genreRepository.addGenresToFilm(id, film.getGenres());
        saveFilmDirectors(id, film.getDirectors());
        return findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));
    }

    @Override
    public List<Film> search(String query, String by) {
        String searchPattern = "%" + query.toLowerCase() + "%";

        String condition;
        Object[] params;

        if (by.contains("director") && by.contains("title")) {
            condition = "(LOWER(d.name) LIKE ? OR LOWER(f.title) LIKE ?)";
            params = new Object[]{searchPattern, searchPattern};
        } else if (by.contains("director")) {
            condition = "LOWER(d.name) LIKE ?";
            params = new Object[]{searchPattern};
        } else {
            condition = "LOWER(f.title) LIKE ?";
            params = new Object[]{searchPattern};
        }

        String finalSql = String.format(SEARCH_FILMS, condition);

        List<Film> films = jdbc.query(finalSql, filmRowMapper, params);

        loadFilmsData(films);

        return films;
    }

    @Override
    public void update(Film film) {
        int rows = jdbc.update(UPDATE,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getRatingId() : null,
                film.getFilmId());
        if (rows == 0) {
            throw new NotFoundException("Фильм с id = " + film.getFilmId() + " не найден.");
        }
        genreRepository.replaceGenres(film.getFilmId(), film.getGenres());
        updateFilmDirectors(film.getFilmId(), film.getDirectors());
    }

    @Override
    public List<Film> findMostPopularWithFilters(int limit, Long genreId, Integer year) {
        Object[] params = new Object[]{genreId, genreId, year, year, limit};
        List<Film> films = jdbc.query(POPULAR_WITH_FILTERS, params, filmRowMapper);
        loadFilmsData(films);
        return films;
    }

    @Override
    public Set<Long> findLikes(Long filmId) {
        return jdbc.queryForList(FIND_LIKES, Long.class, filmId)
                .stream()
                .collect(Collectors.toSet());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(REMOVE_LIKE, filmId, userId);
    }

    @Override
    public boolean deleteById(Long id) {
        genreRepository.deleteGenresByFilmId(id);
        return jdbc.update(DELETE, id) > 0;
    }

    private long insert(String sql, Object... params) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"film_id"});
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return jdbc.queryForList(FIND_LIKES_QUERY, Long.class, filmId)
                .stream().collect(Collectors.toSet());
    }

    @Override
    public List<Film> findByDirector(Long directorId, String sortBy) {
        List<Film> films;

        if ("year".equals(sortBy)) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_ORDER_BY_YEAR, filmRowMapper, directorId);
        } else if ("likes".equals(sortBy)) {
            films = jdbc.query(FIND_FILMS_BY_DIRECTOR_ORDER_BY_LIKES, filmRowMapper, directorId);
        } else {
            throw new IllegalArgumentException("Параметр sortBy должен быть year или likes");
        }

        loadFilmsData(films);
        return films;
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        List<Film> films = jdbc.query(GET_COMMON_FILMS, filmRowMapper, userId, friendId);
        loadFilmsData(films);
        return films;
    }

    @Override
    public List<Film> getRecommendations(Long userId) {
        List<Film> films = jdbc.query(FIND_RECOMMENDATIONS, filmRowMapper, userId, userId, userId);
        loadFilmsData(films);
        return films;
    }

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Long> uniqueDirectorIds = directors.stream()
                .map(Director::getDirectorId)
                .collect(Collectors.toSet());

        for (Long directorId : uniqueDirectorIds) {
            jdbc.update(INSERT_FILM_DIRECTOR_SQL, filmId, directorId);
        }
    }

    private void updateFilmDirectors(Long filmId, Set<Director> directors) {
        jdbc.update(DELETE_FILM_DIRECTORS, filmId);
        saveFilmDirectors(filmId, directors);
    }

    private Set<Director> getDirectorsByFilmId(Long filmId) {
        return new LinkedHashSet<>(jdbc.query(
                FIND_DIRECTORS_BY_FILM_ID,
                (rs, rowNum) -> Director.builder()
                        .directorId(rs.getLong("director_id"))
                        .name(rs.getString("name"))
                        .build(),
                filmId
        ));
    }

    private void checkFilmExists(Long filmId) {
        if (filmId == null || !findById(filmId).isPresent()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    private void loadFilmData(Film film) {
        if (film == null) {
            return;
        }
        film.setGenres(genreRepository.findGenresByFilmId(film.getFilmId()));
        film.setDirectors(getDirectorsByFilmId(film.getFilmId()));
    }

    private void loadFilmsData(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }
        for (Film film : films) {
            loadFilmData(film);
        }
    }
}