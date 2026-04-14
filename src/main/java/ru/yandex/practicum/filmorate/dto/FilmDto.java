package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
public class FilmDto {
    @JsonProperty("id")
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;
    private MpaDto mpa;
    private List<GenreDto> genres;

    @JsonProperty("directors")
    private Set<DirectorDto> directors;
}