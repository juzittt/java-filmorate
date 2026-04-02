package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("FilmController тесты")
public class FilmControllerTest {

    private static final Logger log = LoggerFactory.getLogger(FilmControllerTest.class);

    private final FilmController filmController;
    private final UserService userService;

    private Validator validator;
    private Film createdFilm;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        createdFilm = Film.builder()
                .name("default-film")
                .description("default-description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .mpa(new MpaRating(1L, "G"))
                .duration(100)
                .build();

        ResponseEntity<Film> response = filmController.createFilm(createdFilm);
        createdFilm = response.getBody();

        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.CREATED);
        log.info("Подготовлен базовый фильм: {}", createdFilm.getName());
    }

    @Test
    @DisplayName("Должен добавить фильм, если данные корректны — возвращает 201 OK")
    void shouldAddFilmWhenValid() {
        Film validFilm = Film.builder()
                .name("Валидный фильм")
                .description("Описание фильма")
                .releaseDate(LocalDate.of(1995, 1, 1))
                .mpa(new MpaRating(2L, "PG"))
                .genres(List.of(new Genre(1L, "Комедия")))
                .duration(120)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        ResponseEntity<Film> response = filmController.createFilm(validFilm);
        Film saved = response.getBody();

        log.info("Фильм сохранён: {}", saved.getName());
        assertThat(violations).isEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(saved).isNotNull();
        assertThat(saved.getFilmId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Валидный фильм");
        assertThat(saved.getDuration()).isEqualTo(120);
        assertThat(saved.getMpa().getName()).isEqualTo("PG");
        assertThat(saved.getGenres()).hasSize(1);
        assertThat(saved.getGenres().iterator().next().getName()).isEqualTo("Комедия");
    }

    @Test
    @DisplayName("Должен выдать ошибку валидации, если название пустое")
    void shouldFailValidationWhenNameEmpty() {
        Film invalidFilm = Film.builder()
                .name("")
                .description("Описание")
                .releaseDate(LocalDate.now())
                .mpa(new MpaRating(1L, "G"))
                .duration(100)
                .build();

        Set<ConstraintViolation<Film>> violations = validator.validate(invalidFilm);

        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage())
                .isEqualTo("Название не должно быть пустым");
    }

    @Test
    @DisplayName("Должен выдать ошибку, если дата релиза до 28 декабря 1895 года")
    void shouldFailWhenReleaseDateTooEarly() {
        Film invalidFilm = Film.builder()
                .name("Старый фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .mpa(new MpaRating(1L, "G"))
                .duration(100)
                .build();

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> filmController.createFilm(invalidFilm)
        );

        assertThat(exception.getMessage())
                .isEqualTo("Самая ранняя разрешенная дата релиза — 28 декабря 1895 года.");
    }

    @Test
    @DisplayName("Должен обновить фильм, если он существует — возвращает 200 OK")
    void shouldUpdateExistingFilm() {
        Film updatedFilm = createdFilm.toBuilder()
                .name("Обновлённое имя")
                .duration(150)
                .build();

        ResponseEntity<Film> response = filmController.updateFilm(updatedFilm);
        Film result = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Обновлённое имя");
        assertThat(result.getDuration()).isEqualTo(150);

        ResponseEntity<List<Film>> filmsResponse = filmController.getFilms();
        assertThat(filmsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(filmsResponse.getBody()).contains(result);
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего фильма — возвращает 404")
    void shouldThrowNotFoundWhenUpdateNonExistentFilm() {
        Film fakeFilm = Film.builder()
                .filmId(9999L)
                .name("Не существую")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(new MpaRating(1L, "G"))
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.updateFilm(fakeFilm)
        );

        assertThat(exception.getMessage()).contains("не найден");
    }

    @Test
    @DisplayName("Должен корректно сортировать популярные фильмы по количеству лайков — GET /films/popular")
    void shouldReturnPopularFilmsInCorrectOrder() {
        List<User> users = createTestUsers();
        List<Film> testFilms = createTestFilms();

        long u1 = users.get(0).getUserId();
        long u2 = users.get(1).getUserId();
        long u3 = users.get(2).getUserId();

        long f1 = createdFilm.getFilmId();
        long f2 = testFilms.get(0).getFilmId();
        long f3 = testFilms.get(1).getFilmId();

        filmController.addLike(f2, u1);
        filmController.addLike(f2, u2);
        filmController.addLike(f2, u3);

        filmController.addLike(f3, u1);
        filmController.addLike(f3, u2);

        filmController.addLike(f1, u1);

        ResponseEntity<List<Film>> response = filmController.getPopular(3);
        List<Film> popular = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(popular).hasSize(3);
        assertThat(popular.get(0).getName()).isEqualTo("Популярный фильм");
        assertThat(popular.get(1).getName()).isEqualTo("Средний фильм");
        assertThat(popular.get(2).getName()).isEqualTo("default-film");
    }

    @Test
    @DisplayName("Должен добавить и удалить лайк — возвращает 204 No Content")
    void shouldAddAndRemoveLike() {
        List<User> users = createTestUsers();
        long filmId = createdFilm.getFilmId();
        long userId = users.get(0).getUserId();

        ResponseEntity<Void> addResponse = filmController.addLike(filmId, userId);
        ResponseEntity<Set<Long>> likesAfterAdd = filmController.getLikes(filmId);

        ResponseEntity<Void> removeResponse = filmController.removeLike(filmId, userId);
        ResponseEntity<Set<Long>> likesAfterRemove = filmController.getLikes(filmId);

        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(likesAfterAdd.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(likesAfterAdd.getBody()).contains(userId);

        assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(likesAfterRemove.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(likesAfterRemove.getBody()).doesNotContain(userId).isEmpty();
    }

    @Test
    @DisplayName("Должен выбросить исключение, если фильм не найден при добавлении лайка")
    void shouldThrowNotFoundWhenFilmNotFoundOnLike() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> filmController.addLike(9999L, 1L)
        );
        assertThat(exception.getMessage()).contains("не найден");
    }

    private List<User> createTestUsers() {
        return Arrays.asList(
                userService.addUser(User.builder().email("u1@e.com").login("u1").birthday(LocalDate.now()).build()),
                userService.addUser(User.builder().email("u2@e.com").login("u2").birthday(LocalDate.now()).build()),
                userService.addUser(User.builder().email("u3@e.com").login("u3").birthday(LocalDate.now()).build())
        );
    }

    private List<Film> createTestFilms() {
        Film f2 = Film.builder()
                .name("Популярный фильм")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(new MpaRating(1L, "G"))
                .build();

        Film f3 = Film.builder()
                .name("Средний фильм")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(new MpaRating(2L, "PG"))
                .build();

        ResponseEntity<Film> r1 = filmController.createFilm(f2);
        ResponseEntity<Film> r2 = filmController.createFilm(f3);

        return Arrays.asList(r1.getBody(), r2.getBody());
    }
}