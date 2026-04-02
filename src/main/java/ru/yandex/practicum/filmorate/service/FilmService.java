package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmDbStorage filmStorage;
    private final MpaRatingService mpaService;
    private final GenreService genreService;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film) {
        FilmMapper.validate(film);

        if (film.getMpa() != null && film.getMpa().getRatingId() != null) {
            mpaService.getRatingById(film.getMpa().getRatingId());
        }

        Set<Genre> uniqueGenres = new LinkedHashSet<>();
        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                if (genre.getGenreId() != null) {
                    genreService.validateGenreExists(genre.getGenreId());
                    uniqueGenres.add(genre);
                }
            }
        }
        film.setGenres(new ArrayList<>(uniqueGenres));

        filmStorage.saveFilm(film);

        return filmStorage.getFilmById(film.getFilmId())
                .orElseThrow(() -> new InternalServerException("Фильм не найден после создания"));
    }

    public Film updateFilm(Film newFilm) {
        FilmMapper.validate(newFilm);
        Optional<Film> existing = filmStorage.getFilmById(newFilm.getFilmId());
        if (existing.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + newFilm.getFilmId() + " не найден.");
        }
        filmStorage.saveFilm(newFilm);
        return newFilm;
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getMostPopularFilms(count);
    }

    public Optional<Film> getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Set<Long> getLikes(Long filmId) {
        return filmStorage.getLikes(filmId);
    }

    public void deleteFilm(Long filmId) {
        if (!filmStorage.deleteFilm(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }
}