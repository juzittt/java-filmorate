package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final FilmRepository filmRepository;
    private final GenreDbStorage genreDbStorage;

    @Override
    public void saveFilm(Film film) {
        filmRepository.save(film);

        if (film.getGenres() != null) {
            genreDbStorage.addGenresToFilm(film.getFilmId(), film.getGenres());
        }
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        checkFilmExists(filmId);
        filmRepository.addLike(filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        filmRepository.removeLike(filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        return filmRepository.findMostPopular(count);
    }

    @Override
    public List<Film> getFilms() {
        return filmRepository.findAll();
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return filmRepository.findById(filmId)
                .map(film -> {
                    List<Genre> genres = genreDbStorage.getGenresByFilmId(film.getFilmId());
                    film.setGenres(genres);
                    return film;
                });
    }

    @Override
    public Set<Long> getLikes(Long filmId) {
        return filmRepository.findLikes(filmId);
    }

    private void checkFilmExists(Long filmId) {
        if (filmId == null || !filmRepository.findById(filmId).isPresent()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }
    }

    public boolean deleteFilm(Long filmId) {
        return filmRepository.deleteFilm(filmId);
    }
}