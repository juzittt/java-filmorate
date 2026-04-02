package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("FilmDbStorage тесты")
public class FilmDbStorageTest {

    private static final Logger log = LoggerFactory.getLogger(FilmDbStorageTest.class);

    private final FilmDbStorage storage;
    private final UserDbStorage uStorage;
    private final MpaRatingService mpaService;

    @Test
    @DisplayName("Должен сохранить фильм —  возвращает фильм с ID")
    void shouldSaveFilm() {
        Film film = Film.builder()
                .name("Новый фильм")
                .description("Описание")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .mpa(mpaService.getRatingById(1L))
                .build();

        storage.saveFilm(film);
        Optional<Film> saved = storage.getFilmById(film.getFilmId());

        assertThat(saved).isPresent();
        assertThat(saved.get().getFilmId()).isNotNull();
        assertThat(saved.get().getName()).isEqualTo("Новый фильм");
        log.info("Сохранён фильм: {}", saved.get().getName());
    }

    @Test
    @DisplayName("Должен обновить фильм —  изменения сохранены")
    void shouldUpdateFilm() {
        Film film = Film.builder()
                .name("Старое имя")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(90)
                .mpa(mpaService.getRatingById(1L))
                .build();
        storage.saveFilm(film);

        Film updated = film.toBuilder().name("Обновлённое имя").duration(150).build();
        storage.saveFilm(updated);
        Optional<Film> result = storage.getFilmById(film.getFilmId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Обновлённое имя");
        assertThat(result.get().getDuration()).isEqualTo(150);
    }

    @Test
    @DisplayName("Должен добавить и удалить лайк — likes меняется")
    void shouldAddAndRemoveLike() {
        User user = createUser("test@email.com", "filmorate");
        Film film = createTestFilm();

        storage.addLike(film.getFilmId(), user.getUserId());
        Set<Long> likesAfterAdd = storage.getLikes(film.getFilmId());

        storage.removeLike(film.getFilmId(), user.getUserId());
        Set<Long> likesAfterRemove = storage.getLikes(film.getFilmId());

        assertThat(likesAfterAdd).contains(user.getUserId());
        assertThat(likesAfterRemove).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть топ популярных фильмов —  отсортировано по убыванию")
    void shouldReturnPopularFilmsInOrder() {

        User u1 = createUser("u1@e.com", "user1");
        User u2 = createUser("u2@e.com", "user2");
        User u3 = createUser("u3@e.com", "user3");

        Film f1 = createTestFilmWithName("Фильм A");
        Film f2 = createTestFilmWithName("Фильм B");
        Film f3 = createTestFilmWithName("Фильм C");

        storage.addLike(f2.getFilmId(), u1.getUserId());
        storage.addLike(f2.getFilmId(), u2.getUserId());
        storage.addLike(f2.getFilmId(), u3.getUserId());

        storage.addLike(f1.getFilmId(), u1.getUserId());
        storage.addLike(f1.getFilmId(), u2.getUserId());

        storage.addLike(f3.getFilmId(), u1.getUserId());

        List<Film> popular = storage.getMostPopularFilms(3);

        assertThat(popular).hasSize(3);
        assertThat(popular.get(0).getName()).isEqualTo("Фильм B");
        assertThat(popular.get(1).getName()).isEqualTo("Фильм A");
        assertThat(popular.get(2).getName()).isEqualTo("Фильм C");
    }

    @Test
    @DisplayName("Должен выбросить исключение при удалении несуществующего фильма")
    void shouldThrowNotFoundWhenDeleteNonExistentFilm() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> storage.deleteFilm(999L)
        );
        assertThat(exception.getMessage()).contains("не найден");
    }

    private Film createTestFilm() {
        Film film = Film.builder()
                .name("Тестовый фильм")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(mpaService.getRatingById(1L))
                .build();
        storage.saveFilm(film);
        return film;
    }

    private Film createTestFilmWithName(String name) {
        Film film = Film.builder()
                .name(name)
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(mpaService.getRatingById(1L))
                .build();
        storage.saveFilm(film);
        return film;
    }

    private User createUser(String email, String login) {
        User user = User.builder()
                .name(login)
                .email(email)
                .login(login)
                .birthday(LocalDate.now())
                .build();
        uStorage.saveUser(user);
        return user;
    }
}