package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("GenreController :: HTTP Contract Tests")
public class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GenreService genreService;

    @Test
    @DisplayName("getAll() should return 200 with list of genres")
    void getAll_ShouldReturn200_WithListOfGenres() throws Exception {
        List<GenreDto> genres = List.of(
                GenreDto.builder().id(1L).name("Комедия").build(),
                GenreDto.builder().id(2L).name("Драма").build()
        );
        when(genreService.getAllGenres()).thenReturn(genres);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Комедия"));
    }

    @Test
    @DisplayName("getById() should return 200 when genre exists")
    void getById_ShouldReturn200_WhenGenreExists() throws Exception {
        GenreDto genre = GenreDto.builder().id(1L).name("Комедия").build();
        when(genreService.getGenreById(1L)).thenReturn(genre);

        mockMvc.perform(get("/genres/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Комедия"));
    }

    @Test
    @DisplayName("getById() should return 404 when genre not found")
    void getById_ShouldReturn404_WhenGenreNotFound() throws Exception {
        when(genreService.getGenreById(999L))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Жанр не найден"));

        mockMvc.perform(get("/genres/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Жанр не найден"));
    }
}