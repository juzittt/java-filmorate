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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("GenreDbStorage тесты")
public class GenreDbStorageTest {

    private static final Logger log = LoggerFactory.getLogger(GenreDbStorageTest.class);

    private final GenreDbStorage storage;
    private final FilmDbStorage filmDbStorage;
    private final GenreService genreService;

    @Test
    @DisplayName("Должен вернуть все жанры — список из 6 жанров")
    void shouldGetAllGenres() {
        List<Genre> genres = storage.getAllGenres();

        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getName()).isEqualTo("Боевик");
        log.info("Загружено жанров: {}", genres.size());
    }

    @Test
    @DisplayName("Должен вернуть жанр по ID —  'Мультфильм'")
    void shouldGetGenreById() {
        Optional<Genre> genre = storage.getGenreById(3L);

        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Мультфильм");
    }

    @Test
    @DisplayName("Должен добавить жанры к фильму — getGenresByFilmId возвращает их")
    void shouldAddGenresToFilm() {
        Film film = createTestFilm();
        List<Genre> genres = List.of(
                genreService.getGenreById(1L),
                genreService.getGenreById(2L)
        );

        storage.addGenresToFilm(film.getFilmId(), genres);
        List<Genre> result = storage.getGenresByFilmId(film.getFilmId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма");
    }

    private Film createTestFilm() {
        Film film = Film.builder()
                .name("Жанровый фильм")
                .description("...")
                .releaseDate(LocalDate.now())
                .duration(100)
                .mpa(new ru.yandex.practicum.filmorate.model.MpaRating(1L, "G"))
                .build();
        filmDbStorage.saveFilm(film);
        return film;
    }
}