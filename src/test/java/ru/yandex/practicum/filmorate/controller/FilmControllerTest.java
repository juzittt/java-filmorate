package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты FilmController")
class FilmControllerTest {

    private FilmController filmController;
    private Validator validator;
    private FilmService filmService;
    private UserService userService;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);
        InMemoryFilmStorage filmStorage = new InMemoryFilmStorage();
        filmService = new FilmService(userService, filmStorage);
        filmController = new FilmController(filmService);

        Film defaultFilm = Film.builder()
                .name("default-film")
                .description("default-description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();
        filmController.addFilm(defaultFilm);
    }

    @Test
    @DisplayName("addFilm — валидный фильм — должен добавиться успешно")
    void addFilm_validFilm_success() {
        Film validFilm = Film.builder()
                .name("film")
                .description("_".repeat(200))
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(100)
                .build();

        Film addedFilm = filmController.addFilm(validFilm);

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertTrue(violations.isEmpty());
        assertNotNull(addedFilm.getId());
        assertEquals(validFilm.getName(), addedFilm.getName());
        assertEquals(validFilm.getDescription(), addedFilm.getDescription());
        assertEquals(validFilm.getReleaseDate(), addedFilm.getReleaseDate());
        assertEquals(validFilm.getDuration(), addedFilm.getDuration());
        assertTrue(filmController.getFilms().contains(addedFilm));
    }

    @Test
    @DisplayName("addFilm — название пустое — должен выбросить исключение валидации")
    void addFilm_nameBlank_validationError() {
        Film invalidFilm = Film.builder()
                .name("")
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Название не должно быть пустым", message);
    }

    @Test
    @DisplayName("addFilm — название null — должен выбросить исключение валидации")
    void addFilm_nameNull_validationError() {
        Film invalidFilm = Film.builder()
                .name(null)
                .description("description")
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Название не должно быть пустым", message);
    }

    @Test
    @DisplayName("addFilm — описание длиннее 200 символов — должен выбросить исключение валидации")
    void addFilm_descriptionTooLong_validationError() {
        Film invalidFilm = Film.builder()
                .name("film")
                .description("_".repeat(201))
                .releaseDate(LocalDate.of(1999, 2, 2))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Описание не должно превышать 200 символов", message);
    }

    @Test
    @DisplayName("addFilm — дата релиза до 28.12.1895 — должен выбросить бизнес-исключение")
    void addFilm_releaseDateBeforeMin_businessError() {
        Film invalidFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.addFilm(invalidFilm)
        );
        assertEquals("Самая ранняя разрешенная дата фильма: 1895-12-28", exception.getMessage());
    }

    @Test
    @DisplayName("addFilm — дата релиза null — должен выбросить бизнес-исключение")
    void addFilm_releaseDateNull_businessError() {
        Film invalidFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(null)
                .duration(100)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.addFilm(invalidFilm)
        );
        assertEquals("Самая ранняя разрешенная дата фильма: 1895-12-28", exception.getMessage());
    }

    @Test
    @DisplayName("addFilm — длительность <= 0 — должен выбросить исключение валидации")
    void addFilm_durationNotPositive_validationError() {
        Film invalidFilm = Film.builder()
                .name("film")
                .description("description")
                .releaseDate(LocalDate.of(1895, 12, 28))
                .duration(0)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Продолжительность должна быть положительной", message);
    }

    @Test
    @DisplayName("updateFilm — валидные данные, фильм найден — должен обновиться")
    void updateFilm_validData_filmFound_success() {
        Film updatedFilm = Film.builder()
                .id(1L)
                .name("updated-film")
                .description("updated-desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(150)
                .build();

        Film result = filmController.updateFilm(updatedFilm);

        assertNotNull(result);
        assertEquals(updatedFilm.getName(), result.getName());
        assertEquals(updatedFilm.getDuration(), result.getDuration());
    }

    @Test
    @DisplayName("updateFilm — фильм не найден — должен выбросить NotFoundException")
    void updateFilm_filmNotFound_throwNotFoundException() {
        Film missingFilm = Film.builder()
                .id(999L)
                .name("missing")
                .description("desc")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(missingFilm)
        );
        assertEquals("Фильм с id = 999 не найден.", exception.getMessage());
    }

    @Test
    @DisplayName("getMostPopularFilms — фильмы с лайками — должен вернуть отсортированный список")
    void getMostPopularFilms_withLikes_returnsSortedByLikes() {
        setUpUsers();
        setUpFilms();

        filmController.addLike(1L, 1L);

        filmController.addLike(2L, 1L);
        filmController.addLike(2L, 2L);
        filmController.addLike(2L, 3L);

        filmController.addLike(3L, 1L);
        filmController.addLike(3L, 2L);

        List<Film> popular = filmController.getMostPopularFilms(3);

        assertEquals("film2", popular.get(0).getName());
        assertEquals("film3", popular.get(1).getName());
        assertEquals("default-film", popular.get(2).getName());
    }

    @Test
    @DisplayName("addLike — валидные id — должен добавить лайк")
    void addLike_validIds_likeAdded() {
        setUpUsers();

        filmController.addLike(1L, 1L);
        filmController.addLike(1L, 3L);

        Set<Long> likes = filmService.getLikes(1L);
        assertEquals(2, likes.size());
        assertTrue(likes.contains(1L));
        assertTrue(likes.contains(3L));
    }

    @Test
    @DisplayName("deleteLike — валидные id — должен удалить лайк")
    void deleteLike_validIds_likeRemoved() {
        setUpUsers();
        filmController.addLike(1L, 1L);
        filmController.addLike(1L, 3L);

        filmController.deleteLike(1L, 3L);

        Set<Long> likes = filmService.getLikes(1L);
        assertEquals(1, likes.size());
        assertTrue(likes.contains(1L));
        assertFalse(likes.contains(3L));
    }

    @Test
    @DisplayName("addLike — фильм не найден — должен выбросить NotFoundException")
    void addLike_filmNotFound_throwNotFoundException() {
        setUpUsers();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLike(999L, 1L)
        );
        assertEquals("Фильм с id = 999 не найден.", exception.getMessage());
    }

    @Test
    @DisplayName("addLike — пользователь не найден — должен выбросить NotFoundException")
    void addLike_userNotFound_throwNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLike(1L, 999L)
        );

        assertEquals("Не найдены пользователи с id: [999]", exception.getMessage());
    }

    @Test
    @DisplayName("deleteLike — фильм не найден — должен выбросить NotFoundException")
    void deleteLike_filmNotFound_throwNotFoundException() {
        setUpUsers();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.deleteLike(999L, 1L)
        );
        assertEquals("Фильм с id = 999 не найден.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteLike — пользователь не найден — должен выбросить NotFoundException")
    void deleteLike_userNotFound_throwNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.deleteLike(1L, 999L)
        );

        assertEquals("Не найдены пользователи с id: [999]", exception.getMessage());
    }

    void setUpUsers() {
        for (int i = 1; i <= 3; i++) {
            User user = User.builder()
                    .email("user" + i + "@email.com")
                    .login("login" + i)
                    .name("name" + i)
                    .birthday(LocalDate.of(2000, 1, 1))
                    .build();
            userService.addUser(user);
        }
    }

    void setUpFilms() {
        for (int i = 2; i <= 3; i++) {
            Film film = Film.builder()
                    .name("film" + i)
                    .description("_".repeat(200))
                    .releaseDate(LocalDate.of(1895, 12, 28))
                    .duration(100)
                    .build();
            filmController.addFilm(film);
        }
    }
}