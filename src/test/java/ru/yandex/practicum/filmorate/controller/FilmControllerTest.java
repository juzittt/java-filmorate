package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
@DisplayName("Интеграционные тесты FilmController")
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    @MockBean
    private UserService userService;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = Film.builder()
                .filmId(1L)
                .name("Valid Film")
                .description("Good film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
    }

    @Test
    @DisplayName("GET /films — должен вернуть список фильмов")
    void getFilms_shouldReturnFilmList() throws Exception {
        when(filmService.getFilms()).thenReturn(List.of(validFilm));

        mockMvc.perform(get("/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Valid Film"));

        verify(filmService, times(1)).getFilms();
    }

    @Test
    @DisplayName("POST /films — валидный фильм — должен вернуть 201 и сохранить")
    void addFilm_validFilm_shouldReturnCreated() throws Exception {
        when(filmService.addFilm(any(Film.class))).thenReturn(validFilm);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Valid Film"));

        verify(filmService, times(1)).addFilm(any(Film.class));
    }

    @Test
    @DisplayName("POST /films — пустое название — должен вернуть 400")
    void addFilm_nameBlank_shouldReturnBadRequest() throws Exception {
        Film invalidFilm = validFilm.toBuilder().name("").build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    @DisplayName("PUT /films — валидное обновление — должен вернуть 200")
    void updateFilm_validData_shouldReturnOk() throws Exception {
        when(filmService.updateFilm(any(Film.class))).thenReturn(validFilm);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Valid Film"));

        verify(filmService, times(1)).updateFilm(any(Film.class));
    }

    @Test
    @DisplayName("PUT /films — фильм не найден — должен вернуть 404")
    void updateFilm_notFound_shouldReturnNotFound() throws Exception {
        when(filmService.updateFilm(any(Film.class)))
                .thenThrow(new NotFoundException("Фильм не найден"));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"));
    }

    @Test
    @DisplayName("PUT /films/{id}/like/{userId} — добавление лайка — должен вернуть 204")
    void addLike_validIds_shouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/films/1/like/5"))
                .andExpect(status().isNoContent());

        verify(filmService, times(1)).addLike(1L, 5L);
    }

    @Test
    @DisplayName("DELETE /films/{id}/like/{userId} — удаление лайка — должен вернуть 204")
    void deleteLike_validIds_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/films/1/like/5"))
                .andExpect(status().isNoContent());

        verify(filmService, times(1)).deleteLike(1L, 5L);
    }

    @Test
    @DisplayName("GET /films/popular?count=3 — должен вернуть 3 фильма")
    void getMostPopularFilms_withCount_shouldReturnN() throws Exception {
        // Given
        List<Film> films = List.of(
                Film.builder().filmId(1L).name("Top 1").build(),
                Film.builder().filmId(2L).name("Top 2").build(),
                Film.builder().filmId(3L).name("Top 3").build()
        );
        when(filmService.getMostPopularFilms(3)).thenReturn(films);

        // When & Then
        mockMvc.perform(get("/films/popular").param("count", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("Top 1"))
                .andExpect(jsonPath("$[1].name").value("Top 2"))
                .andExpect(jsonPath("$[2].name").value("Top 3"));

        verify(filmService).getMostPopularFilms(3);
    }

    @Test
    @DisplayName("GET /films/popular?count=0 — должен вернуть пустой список")
    void getMostPopularFilms_countZero_shouldReturnEmpty() throws Exception {
        when(filmService.getMostPopularFilms(0)).thenReturn(List.of());

        mockMvc.perform(get("/films/popular").param("count", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /films/1/likes — должен вернуть список ID лайкнувших")
    void getLikes_filmExists_withLikes_shouldReturnUserIds() throws Exception {
        // Given
        Set<Long> likes = Set.of(101L, 102L, 103L);
        when(filmService.getLikes(1L)).thenReturn(likes);

        // When & Then
        mockMvc.perform(get("/films/1/likes"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(hasItems(101, 102, 103)));
    }

    @Test
    @DisplayName("GET /films/2/likes — без лайков — должен вернуть пустой массив")
    void getLikes_noLikes_shouldReturnEmptySet() throws Exception {
        when(filmService.getLikes(2L)).thenReturn(Collections.emptySet());

        mockMvc.perform(get("/films/2/likes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /films/999/likes — фильм не найден — должен вернуть 404")
    void getLikes_filmNotFound_shouldReturnNotFound() throws Exception {
        when(filmService.getLikes(999L)).thenThrow(new NotFoundException("Фильм с id = 999 не найден."));

        mockMvc.perform(get("/films/999/likes"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм с id = 999 не найден."));
    }
}