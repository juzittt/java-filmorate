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
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("MpaRatingController тесты")
public class MpaRatingControllerTest {

    private static final Logger log = LoggerFactory.getLogger(MpaRatingControllerTest.class);

    private final MpaRatingController controller;

    @Test
    @DisplayName("Должен вернуть все рейтинги — 200 OK и список")
    void shouldReturnAllRatings() {
        ResponseEntity<List<MpaRating>> response = controller.getAll();
        List<MpaRating> ratings = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0).getName()).isEqualTo("G");
        log.info("Получено рейтингов: {}", ratings.size());
    }

    @Test
    @DisplayName("Должен вернуть рейтинг по ID — 200 OK и 'G'")
    void shouldReturnRatingById() {
        ResponseEntity<MpaRating> response = controller.getById(1L);
        MpaRating rating = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rating).isNotNull();
        assertThat(rating.getRatingId()).isEqualTo(1L);
        assertThat(rating.getName()).isEqualTo("G");
    }
}