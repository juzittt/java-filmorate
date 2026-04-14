package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DirectorDto {
    @JsonProperty("id")
    private Long director_id;

    @NotNull(message = "Имя режиссера не должно быть пустым")
    private String name;
}
