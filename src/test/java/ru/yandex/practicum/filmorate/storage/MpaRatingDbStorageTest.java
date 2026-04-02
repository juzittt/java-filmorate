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
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("MpaRatingDbStorage тесты")
public class MpaRatingDbStorageTest {

    private static final Logger log = LoggerFactory.getLogger(MpaRatingDbStorageTest.class);

    private final MpaRatingDbStorage storage;

    @Test
    @DisplayName("Должен вернуть все рейтинги — список из 5 элементов")
    void shouldGetAllRatings() {
        List<MpaRating> ratings = storage.getAllRatings();

        assertThat(ratings).hasSize(5);
        assertThat(ratings.get(0).getName()).isEqualTo("G");
        log.info("Загружено рейтингов: {}", ratings.size());
    }

    @Test
    @DisplayName("Должен вернуть рейтинг по ID — 'PG'")
    void shouldGetRatingById() {
        Optional<MpaRating> rating = storage.getRatingById(2L);

        assertThat(rating).isPresent();
        assertThat(rating.get().getName()).isEqualTo("PG");
    }
}