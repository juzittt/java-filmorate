package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    private final UserService userService;
    private final FilmStorage filmStorage;

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film addFilm(Film film) {
        validateFilm(film);

        Long id = (long) (getFilms().size() + 1);
        film.setFilmId(id);
        filmStorage.saveFilm(film);
        log.info("Film with name: {} was added.", film.getName());
        return film;
    }

    public Film updateFilm(Film newFilm) {
        validateFilm(newFilm);

        return filmStorage.getFilmById(newFilm.getFilmId())
                .map(film -> processUpdateFilm(newFilm))
                .orElseThrow(() -> {
                    log.error("Error updating film: {}, with id: {}", newFilm.getName(), newFilm.getFilmId());
                    return new NotFoundException("Фильм с id = " + newFilm.getFilmId() + " не найден.");
                });
    }

    public void addLike(Long filmId, Long userId) {
        checkPresence(filmId, userId);
        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        checkPresence(filmId, userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(Integer count) {
        if (count == null) {
            count = 10;
        }
        return filmStorage.getMostPopularFilms(count);
    }

    public Set<Long> getLikes(Long filmId) {
        checkFilmPresence(filmId);
        return filmStorage.getLikes(filmId);
    }

    private void checkPresence(Long filmId, Long userId) {
        checkFilmPresence(filmId);
        userService.checkUserPresence(userId);
    }

    private void checkFilmPresence(Long filmId) {
        filmStorage.getFilmById(filmId).orElseThrow(() -> {
            log.error("Error checking film presence: {}, with id: {}", filmId, filmId);
            return new NotFoundException("Фильм с id = " + filmId + " не найден.");
        });
    }

    private Film processUpdateFilm(Film newFilm) {
        filmStorage.saveFilm(newFilm);
        log.info("Updated film with id: {}", newFilm.getFilmId());
        return newFilm;
    }

    private void validateFilm(Film film) {
        if (isWrongDate(film)) {
            log.error("Film with id = {}. Wrong release date: {}", film.getFilmId(), film.getReleaseDate());
            throw new ValidationException("Самая ранняя разрешенная дата фильма: 1895-12-28");
        }
    }

    private boolean isWrongDate(Film film) {
        return film.getReleaseDate() == null
                || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28));
    }
}
