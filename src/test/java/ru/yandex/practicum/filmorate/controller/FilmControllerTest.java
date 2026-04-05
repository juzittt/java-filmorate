package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.testdata.TestData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("FilmController :: HTTP Contract Tests")
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    private Film film;
    private FilmDto filmDto;

    @BeforeEach
    void setUp() {
        film = TestData.createFilm();
        filmDto = TestData.createFilmDto(2L);
    }

    @Test
    @DisplayName("createFilm() should return 201 when valid")
    void createFilm_ShouldReturn201_WhenValid() throws Exception {
        film.setFilmId(1L);
        when(filmService.createFilm(any())).thenReturn(filmDto);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value(TestData.VALID_FILM_NAME));
    }

    @Test
    @DisplayName("updateFilm() should return 200 when exists")
    void updateFilm_ShouldReturn200_WhenExists() throws Exception {
        film.setFilmId(1L);
        when(filmService.updateFilm(any())).thenReturn(filmDto);

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    @DisplayName("updateFilm() should throw NotFoundException when not found")
    void updateFilm_ShouldThrowNotFound_WhenNotFound() throws Exception {
        film.setFilmId(999L);
        doThrow(new NotFoundException("Фильм не найден"))
                .when(filmService).updateFilm(any());

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Фильм не найден"));
    }
}