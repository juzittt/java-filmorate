package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
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
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("GenreController тесты")
public class GenreControllerTest {

    private static final Logger log = LoggerFactory.getLogger(GenreControllerTest.class);

    private final GenreController genreController;

    @Test
    @DisplayName("Должен вернуть список всех жанров — 200 OK и список")
    void shouldReturnAllGenres() {
        ResponseEntity<List<Genre>> response = genreController.getAll();
        List<Genre> genres = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(genres).hasSize(6);
        assertThat(genres.get(0).getName()).isEqualTo("Боевик");
        log.info("Получено жанров: {}", genres.size());
    }

    @Test
    @DisplayName("Должен вернуть жанр по ID — 200 OK и 'Комедия'")
    void shouldReturnGenreById() {
        ResponseEntity<Genre> response = genreController.getById(1L);
        Genre genre = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(genre).isNotNull();
        assertThat(genre.getGenreId()).isEqualTo(1L);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }
}