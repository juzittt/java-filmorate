package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final DirectorMapper directorMapper;

    public List<DirectorDto> getDirectors() {
        return directorRepository.findAll().stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toList());
    }

    public DirectorDto getDirectorById(Long id) {
        return directorMapper.toDto(getDirectorEntity(id));
    }

    public DirectorDto createDirector(DirectorDto request) {
        validateDirector(request);
        Director director = directorMapper.toEntity(request);
        directorRepository.create(director);
        return getDirectorById(director.getDirectorId());
    }

    public DirectorDto updateDirector(DirectorDto request) {
        if (request.getDirector_id() == null) {
            throw new ValidationException("Id режиссера должен быть указан.");
        }

        validateDirector(request);

        Director director = getDirectorEntity(request.getDirector_id());
        directorMapper.updateDirectorFromDto(director, request);
        directorRepository.update(director);

        return getDirectorById(director.getDirectorId());
    }

    public void deleteDirector(Long id) {
        directorRepository.deleteById(id);
    }

    private void validateDirector(DirectorDto request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationException("Имя режиссера не должно быть пустым.");
        }
    }

    private Director getDirectorEntity(Long id) {
        return directorRepository.findById(id);
    }
}