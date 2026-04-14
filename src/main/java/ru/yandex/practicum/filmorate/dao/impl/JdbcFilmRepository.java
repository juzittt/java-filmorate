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
    private static final String POPULAR = """
        SELECT f.film_id, f.title, f.description, f.release_date, f.duration,
               f.rating_id, mr.name AS mpa_name, COUNT(l.user_id) AS like_count
        FROM films f
        LEFT JOIN likes l ON f.film_id = l.film_id
        LEFT JOIN mpa_rating mr ON f.rating_id = mr.rating_id
        GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, mr.name
        ORDER BY like_count DESC
        LIMIT ?
        """;
    private static final String FIND_LIKES = "SELECT user_id FROM likes WHERE film_id = ?";
    private static final String ADD_LIKE = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
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

    private static final String FIND_FILMS_BY_DIRECTOR_ORDER_BY_LIKES = FIND_ALL +
    """
    JOIN film_directors fd ON f.film_id = fd.film_id
    LEFT JOIN likes l ON f.film_id = l.film_id
    WHERE fd.director_id = ?
    GROUP BY f.film_id, f.title, f.description, f.release_date, f.duration, f.rating_id, mr.name
    ORDER BY COUNT(l.user_id) DESC, f.film_id ASC
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
    public List<Film> findMostPopular(int count) {
        List<Film> films = jdbc.query(POPULAR, filmRowMapper, count);
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

    private void saveFilmDirectors(Long filmId, Set<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }

        Set<Long> uniqueDirectorIds = directors.stream()
                .filter(Objects::nonNull)
                .map(Director::getDirectorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

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