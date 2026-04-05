package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class NewFilmRequest {
    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    private LocalDate releaseDate;

    @NotNull(message = "Продолжительность должна быть положительной")
    @Positive(message = "Продолжительность должна быть положительной")
    private Integer duration;

    private MpaDto mpa;
    private List<GenreDto> genres;
}