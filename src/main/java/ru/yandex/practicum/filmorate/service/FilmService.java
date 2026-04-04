package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.dao.MpaRatingRepository;
import ru.yandex.practicum.filmorate.model.Genre;

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
    }

    public void removeLike(Long filmId, Long userId) {
        filmRepository.removeLike(filmId, userId);
    }

    public List<FilmDto> getPopularFilms(int count) {
        return filmRepository.findMostPopular(count).stream()
                .map(this::enrichAndToDto)
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
}