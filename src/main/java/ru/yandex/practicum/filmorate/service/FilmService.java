package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmDbStorage filmStorage;
    private final MpaRatingDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;
    private final FilmMapper filmMapper;

    public FilmDto createFilm(NewFilmRequest request) {
        validateFilmRequest(request);
        Film film = filmMapper.toEntity(request);
        filmStorage.saveFilm(film);
        linkGenres(film.getFilmId(), request.getGenres());
        return filmMapper.toDto(enrichWithGenres(film));
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        Long id = request.getId();
        Film existing = getFilmEntityById(id);
        validateFilmRequest(request);

        filmMapper.updateEntityFromRequest(existing, request);
        filmStorage.saveFilm(existing);
        linkGenres(existing.getFilmId(), request.getGenres());

        return filmMapper.toDto(enrichWithGenres(existing));
    }

    public List<FilmDto> getFilms() {
        return filmStorage.getFilms().stream()
                .map(this::enrichWithGenres)
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(Long id) {
        Film film = getFilmEntityById(id);
        enrichWithGenres(film);
        return filmMapper.toDto(film);
    }

    public void addLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        return filmStorage.getMostPopularFilms(count).stream()
                .map(this::enrichWithGenres)
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public Set<Long> getLikes(Long filmId) {
        checkFilmExists(filmId);
        return filmStorage.getLikes(filmId);
    }

    public void deleteFilm(Long filmId) {
        if (!filmStorage.deleteFilm(filmId)) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }


    private Film getFilmEntityById(Long id) {
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));
    }

    private void checkFilmExists(Long filmId) {
        if (filmId == null || !filmStorage.getFilmById(filmId).isPresent()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    private void validateFilmRequest(NewFilmRequest request) {
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void validateFilmRequest(UpdateFilmRequest request) {
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void doValidate(LocalDate releaseDate, MpaDto mpaDto, List<GenreDto> genreDto) {
        if (releaseDate != null) {
            if (releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
            }
        }

        if (mpaDto != null) {
            mpaStorage.getRatingById(mpaDto.getId())
                    .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + mpaDto.getId() + " не найден."));
        }

        if (genreDto != null) {
            for (GenreDto genre : genreDto) {
                if (genre.getId() != null) {
                    genreStorage.getGenreById(genre.getId())
                            .orElseThrow(() -> new NotFoundException("Жанр с id = " + genre.getId() + " не найден."));
                }
            }
        }
    }

    private void linkGenres(Long filmId, List<GenreDto> genres) {
        if (genres != null) {
            List<ru.yandex.practicum.filmorate.model.Genre> entities = genres.stream()
                    .map(dto -> ru.yandex.practicum.filmorate.model.Genre.builder()
                            .genreId(dto.getId())
                            .name(dto.getName())
                            .build())
                    .collect(Collectors.toList());
            genreStorage.addGenresToFilm(filmId, entities);
        }
    }

    private Film enrichWithGenres(Film film) {
        List<ru.yandex.practicum.filmorate.model.Genre> genres = genreStorage.getGenresByFilmId(film.getFilmId());
        film.setGenres(genres);
        return film;
    }
}