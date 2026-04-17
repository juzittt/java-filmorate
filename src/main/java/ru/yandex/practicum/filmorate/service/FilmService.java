package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.dao.impl.JdbcUserRepository;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.dao.MpaRatingRepository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;
    private final MpaRatingRepository mpaRepository;
    private final GenreRepository genreRepository;
    private final FilmMapper filmMapper;
    private final DirectorRepository directorRepository;
    private final EventService eventService;
    private final JdbcUserRepository userRepository;

    public FilmDto createFilm(NewFilmRequest request) {
        validateNewFilmRequest(request);
        Film film = filmMapper.toEntity(request);
        filmRepository.save(film);
        linkGenres(film.getFilmId(), request.getGenres());
        return getFilmById(film.getFilmId());
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        validateUpdateFilmRequest(request);
        Film film = getFilmEntity(request.getId());
        filmMapper.updateFilmFromRequest(film, request);
        filmRepository.update(film);
        linkGenres(film.getFilmId(), request.getGenres());
        return getFilmById(film.getFilmId());
    }

    public List<FilmDto> getFilms() {
        return filmRepository.findAll().stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(Long id) {
        return enrichAndToDto(getFilmEntity(id));
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmRepository.findById(filmId).isPresent()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
        filmRepository.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmRepository.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        if (userId == null || userId <= 0) {
            throw new ValidationException("Некорректный id пользователя: " + userId);
        }
        if (friendId == null || friendId <= 0) {
            throw new ValidationException("Некорректный id друга: " + friendId);
        }

        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!userRepository.findById(friendId).isPresent()) {
            throw new NotFoundException("Друг с id = " + friendId + " не найден");
        }

        return filmRepository.getCommonFilms(userId, friendId).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteFilm(Long id) {
        if (!filmRepository.deleteById(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
    }

    public Set<Long> getLikes(Long filmId) {
        return filmRepository.findLikes(filmId);
    }

    private void validateNewFilmRequest(NewFilmRequest request) {
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void validateUpdateFilmRequest(UpdateFilmRequest request) {
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void doValidate(LocalDate releaseDate, MpaDto mpaDto, List<GenreDto> genreDtos) {
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (mpaDto != null) {
            mpaRepository.findById(mpaDto.getId())
                    .orElseThrow(() -> new NotFoundException(
                            "Рейтинг MPA с id = " + mpaDto.getId() + " не найден."));
        }

        if (genreDtos != null) {
            for (GenreDto genre : genreDtos) {
                if (genre.getId() != null) {
                    genreRepository.findById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id = " + genre.getId() + " не найден."));
                }
            }
        }
    }

    private Film getFilmEntity(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));
    }

    private FilmDto enrichAndToDto(Film film) {
        film.setGenres(genreRepository.findGenresByFilmId(film.getFilmId()));
        return filmMapper.toDto(film);
    }

    private void linkGenres(Long filmId, List<GenreDto> genres) {
        if (genres == null || genres.isEmpty()) {
            genreRepository.deleteGenresByFilmId(filmId);
            return;
        }

        List<Genre> genreEntities = genres.stream()
                .map(filmMapper::toEntity)
                .collect(Collectors.toList());

        genreRepository.replaceGenres(filmId, genreEntities);
    }

    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        if (!directorRepository.existsById(directorId)) {
            throw new NotFoundException("Режиссер с id = " + directorId + " не найден.");
        }

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Параметр sortBy должен быть year или likes.");
        }

        return filmRepository.findByDirector(directorId, sortBy).stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
    }

    public List<FilmDto> getPopularFilms(int count, Long genreId, Integer year) {

        if (year != null && (year < 1895 || year > LocalDate.now().getYear())) {
            throw new ValidationException("Год должен быть от 1895 до текущего");
        }
        if (genreId != null) {
            genreRepository.findById(genreId)
                    .orElseThrow(() -> new NotFoundException("Жанр с id = " + genreId + " не найден"));
        }

        return filmRepository.findMostPopularWithFilters(count, genreId, year).stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
    }
}