package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    @JsonProperty("id")
    private Long filmId;

    @NotBlank(message = "Название не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Описание не должно превышать 200 символов")
    private String description;

    @NotNull(message = "Дата релиза не должна быть пустой")
    @PastOrPresent(message = "Дата релиза не может быть в будущем")
    private LocalDate releaseDate;

    @NotNull(message = "Длительность не должна быть пустой")
    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    @JsonProperty("mpa")
    private MpaRating mpa;

    @Builder.Default
    @JsonProperty("genres")
    private List<Genre> genres = new ArrayList<>();

    @JsonProperty("directors")
    @NotNull(message = "Режиссёр у фильма обязан быть")
    private Set<Director> directors = new LinkedHashSet<>();
}