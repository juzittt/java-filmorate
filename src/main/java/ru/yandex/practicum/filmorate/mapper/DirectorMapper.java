package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.model.Director;

@Component
public class DirectorMapper {

    public DirectorDto toDto(Director director) {
        return DirectorDto.builder()
                .directorId(director.getDirectorId())
                .name(director.getName())
                .build();
    }

    public Director toEntity(DirectorDto dto) {
        return Director.builder()
                .directorId(dto.getDirectorId())
                .name(dto.getName())
                .build();
    }

    public void updateDirectorFromDto(Director director, DirectorDto dto) {
        director.setName(dto.getName());
    }
}