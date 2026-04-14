package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class NewReviewRequest {
    @NotBlank(message = "Содержимое отзыва обязательно")
    private String content;

    @NotNull(message = "Оценка обязательна")
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Integer useful;
}
