package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FilmRepository {
    List<Film> findAll();

    Optional<Film> findById(Long id);

    Film save(Film film);

    void update(Film film);

    Set<Long> findLikes(Long filmId);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    boolean deleteById(Long id);

    Set<Long> getLikes(Long filmId);

    List<Film> findByDirector(Long directorId, String sortBy);

    List<Film> getRecommendations(Long userId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> findMostPopularWithFilters(int limit, Long genreId, Integer year);
}