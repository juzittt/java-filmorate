package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MpaRatingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("MpaRatingController :: HTTP Contract Tests")
public class MpaRatingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MpaRatingService mpaRatingService;

    @Test
    @DisplayName("getAll() should return 200 with all ratings")
    void getAll_ShouldReturn200_WithAllRatings() throws Exception {
        List<MpaDto> ratings = List.of(
                MpaDto.builder().id(1L).name("G").build(),
                MpaDto.builder().id(2L).name("PG").build()
        );
        when(mpaRatingService.getAllRatings()).thenReturn(ratings);

        mockMvc.perform(get("/mpa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("G"));
    }

    @Test
    @DisplayName("getById() should return 200 when rating exists")
    void getById_ShouldReturn200_WhenRatingExists() throws Exception {
        MpaDto rating = MpaDto.builder().id(1L).name("G").build();
        when(mpaRatingService.getRatingById(1L)).thenReturn(rating);

        mockMvc.perform(get("/mpa/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));
    }

    @Test
    @DisplayName("getById() should return 404 when rating not found")
    void getById_ShouldReturn404_WhenRatingNotFound() throws Exception {
        when(mpaRatingService.getRatingById(999L))
                .thenThrow(new ru.yandex.practicum.filmorate.exception.NotFoundException("Рейтинг не найден"));

        mockMvc.perform(get("/mpa/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Рейтинг не найден"));
    }
}