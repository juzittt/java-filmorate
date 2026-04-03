package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserDto {
    @JsonProperty("id")
    private Long id;
    private String email;
    private String login;
    private String name;
    private LocalDate birthday;
}