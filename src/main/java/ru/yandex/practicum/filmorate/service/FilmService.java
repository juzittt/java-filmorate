package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.dao.impl.JdbcUserRepository;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.mapstruct.FilmMapper;
import ru.yandex.practicum.filmorate.mapper.mapstruct.GenreMapper;
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

@Slf4j
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

    private final GenreMapper genreMapper;

    public FilmDto createFilm(NewFilmRequest request) {
        log.info("Creating new film: title='{}', releaseDate={}", request.getName(), request.getReleaseDate());
        validateNewFilmRequest(request);
        Film film = filmMapper.toEntity(request);
        filmRepository.save(film);
        linkGenres(film.getFilmId(), request.getGenres());
        log.info("Film created successfully with id={}", film.getFilmId());
        return getFilmById(film.getFilmId());
    }

    public FilmDto updateFilm(UpdateFilmRequest request) {
        log.info("Updating film with id={}", request.getId());
        validateUpdateFilmRequest(request);
        Film film = getFilmEntity(request.getId());
        filmMapper.updateFilmFromRequest(request, film);
        filmRepository.update(film);
        linkGenres(film.getFilmId(), request.getGenres());
        log.info("Film updated successfully: id={}, title='{}'", film.getFilmId(), film.getName());
        return getFilmById(film.getFilmId());
    }

    public List<FilmDto> getFilms() {
        log.info("Fetching all films");
        List<FilmDto> films = filmRepository.findAll().stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
        log.info("Found {} films", films.size());
        return films;
    }

    public List<FilmDto> searchFilms(String query, String by) {
        log.info("Searching films by '{}', query: {}", by, query);
        List<Film> films = filmRepository.search(query, by);
        log.info("Found {} films matching query '{}'", films.size(), query);
        return films.stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public FilmDto getFilmById(Long id) {
        log.info("Fetching film with id={}", id);
        FilmDto film = enrichAndToDto(getFilmEntity(id));
        log.info("Film found: id={}, title='{}'", film.getId(), film.getName());
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Adding like: user={} -> film={}", userId, filmId);

        validateFilmId(filmId);
        validateUserId(userId);

        filmRepository.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Removing like: user={} from film={}", userId, filmId);

        validateFilmId(filmId);
        validateUserId(userId);

        filmRepository.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<FilmDto> getCommonFilms(Long userId, Long friendId) {
        log.info("Fetching common films for users: {} and {}", userId, friendId);

        validatePositiveId(userId, "userId");
        validatePositiveId(friendId, "friendId");

        validateUserId(userId);
        validateUserId(friendId);

        List<FilmDto> commonFilms = filmRepository.getCommonFilms(userId, friendId).stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} common films between users {} and {}", commonFilms.size(), userId, friendId);
        return commonFilms;
    }

    public void deleteFilm(Long id) {
        log.info("Deleting film with id={}", id);
        validateFilmId(id);
        boolean deleted = filmRepository.deleteById(id);
        if (!deleted) {
            log.warn("Film deletion failed: film with id={} not found", id);
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
        log.info("Film with id={} deleted successfully", id);
    }

    public Set<Long> getLikes(Long filmId) {
        log.info("Fetching likes for film with id={}", filmId);
        validateFilmId(filmId);
        Set<Long> likes = filmRepository.findLikes(filmId);
        log.info("Film {} has {} likes", filmId, likes.size());
        return likes;
    }

    private void validateNewFilmRequest(NewFilmRequest request) {
        log.debug("Validating NewFilmRequest: title='{}', releaseDate={}", request.getName(), request.getReleaseDate());
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void validateUpdateFilmRequest(UpdateFilmRequest request) {
        log.debug("Validating UpdateFilmRequest: id={}", request.getId());
        doValidate(request.getReleaseDate(), request.getMpa(), request.getGenres());
    }

    private void doValidate(LocalDate releaseDate, MpaDto mpaDto, List<GenreDto> genreDtos) {
        if (releaseDate != null && releaseDate.isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Validation failed: release date {} is before 1895-12-28", releaseDate);
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }

        if (mpaDto != null) {
            mpaRepository.findById(mpaDto.getId())
                    .orElseThrow(() -> {
                        log.warn("MPA rating with id={} not found", mpaDto.getId());
                        return new NotFoundException("Рейтинг MPA с id = " + mpaDto.getId() + " не найден.");
                    });
        }

        if (genreDtos != null) {
            for (GenreDto genre : genreDtos) {
                if (genre.getId() != null) {
                    genreRepository.findById(genre.getId())
                            .orElseThrow(() -> {
                                log.warn("Genre with id={} not found", genre.getId());
                                return new NotFoundException("Жанр с id = " + genre.getId() + " не найден.");
                            });
                }
            }
        }
    }

    private Film getFilmEntity(Long id) {
        return filmRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Film with id={} not found", id);
                    return new NotFoundException("Фильм с id = " + id + " не найден.");
                });
    }

    private FilmDto enrichAndToDto(Film film) {
        log.debug("Enriching film {} with genres", film.getFilmId());
        film.setGenres(genreRepository.findGenresByFilmId(film.getFilmId()));
        return filmMapper.toDto(film);
    }

    private void linkGenres(Long filmId, List<GenreDto> genres) {
        log.debug("Linking genres to film {}: count={}", filmId, genres != null ? genres.size() : 0);
        if (genres == null || genres.isEmpty()) {
            genreRepository.deleteGenresByFilmId(filmId);
            log.info("All genres unlinked from film {}", filmId);
            return;
        }

        List<Genre> genreEntities = genres.stream()
                .map(genreMapper::toEntity)
                .collect(Collectors.toList());

        genreRepository.replaceGenres(filmId, genreEntities);
        log.debug("Genres updated for film {}: {} genre(s) linked", filmId, genreEntities.size());
    }

    public List<FilmDto> getFilmsByDirector(Long directorId, String sortBy) {
        log.info("Fetching films by director id={}, sorted by '{}'", directorId, sortBy);

        validatePositiveId(directorId, "directorId");

        if (!directorRepository.existsById(directorId)) {
            log.warn("Director with id={} not found", directorId);
            throw new NotFoundException("Режиссер с id = " + directorId + " не найден.");
        }

        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            log.warn("Invalid sortBy parameter: '{}'. Expected 'year' or 'likes'", sortBy);
            throw new ValidationException("Параметр sortBy должен быть year или likes.");
        }

        List<FilmDto> films = filmRepository.findByDirector(directorId, sortBy).stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
        log.info("Found {} films directed by {}", films.size(), directorId);
        return films;
    }

    public List<FilmDto> getPopularFilms(int count, Long genreId, Integer year) {
        log.info("Fetching popular films: count={}, genreId={}, year={}", count, genreId, year);

        if (year != null && (year < 1895 || year > LocalDate.now().getYear())) {
            log.warn("Invalid year provided: {}. Must be between 1895 and current year.", year);
            throw new ValidationException("Год должен быть от 1895 до текущего");
        }

        if (genreId != null) {
            validatePositiveId(genreId, "genreId");
            genreRepository.findById(genreId)
                    .orElseThrow(() -> {
                        log.warn("Genre with id={} not found", genreId);
                        return new NotFoundException("Жанр с id = " + genreId + " не найден");
                    });
        }

        List<FilmDto> films = filmRepository.findMostPopularWithFilters(count, genreId, year).stream()
                .map(this::enrichAndToDto)
                .collect(Collectors.toList());
        log.info("Returned {} popular films", films.size());
        return films;
    }

    private void validateUserId(Long userId) {
        log.debug("Validating user existence: id={}", userId);
        if (!userRepository.findById(userId).isPresent()) {
            log.warn("User with id={} not found", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateFilmId(Long filmId) {
        log.debug("Validating film existence: id={}", filmId);
        if (!filmRepository.findById(filmId).isPresent()) {
            log.warn("Film not found: filmId={}", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    private void validatePositiveId(Long id, String paramName) {
        log.debug("Validating {} is positive: {}={}", paramName, paramName, id);
        if (id == null || id <= 0) {
            log.warn("Invalid {} value={}", paramName, id);
            throw new ValidationException("Некорректный " + paramName + ": " + id);
        }
    }
}