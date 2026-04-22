package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.mapstruct.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

    private final DirectorRepository directorRepository;
    private final DirectorMapper directorMapper;

    public List<DirectorDto> getDirectors() {
        log.info("Fetching all directors");
        List<DirectorDto> directors = directorRepository.findAll().stream()
                .map(directorMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} directors", directors.size());
        return directors;
    }

    public DirectorDto getDirectorById(Long id) {
        log.info("Fetching director with id={}", id);
        DirectorDto director = directorMapper.toDto(getDirectorEntity(id));
        log.info("Director found: id={}, name='{}'", director.getDirectorId(), director.getName());
        return director;
    }

    public DirectorDto createDirector(DirectorDto request) {
        log.info("Creating new director: {}", request.getName());
        validateDirector(request);
        Director director = directorMapper.toEntity(request);
        directorRepository.create(director);
        DirectorDto result = getDirectorById(director.getDirectorId());
        log.info("Director created successfully with id={}", result.getDirectorId());
        return result;
    }

    public DirectorDto updateDirector(DirectorDto request) {
        Long id = request.getDirectorId();
        log.info("Updating director with id={}", id);

        if (id == null) {
            log.warn("Attempt to update director without id");
            throw new ValidationException("Id режиссера должен быть указан.");
        }

        validateDirector(request);
        Director director = getDirectorEntity(id);
        directorMapper.updateDirectorFromDto(director, request);
        directorRepository.update(director);

        DirectorDto result = getDirectorById(id);
        log.info("Director updated successfully: id={}, name='{}'", result.getDirectorId(), result.getName());
        return result;
    }

    public void deleteDirector(Long id) {
        log.info("Deleting director with id={}", id);
        directorRepository.deleteById(id);
        log.info("Director with id={} deleted successfully", id);
    }

    private void validateDirector(DirectorDto request) {
        if (request.getName() == null || request.getName().isBlank()) {
            log.warn("Validation failed: director name is blank or null");
            throw new ValidationException("Имя режиссера не должно быть пустым.");
        }
    }

    private Director getDirectorEntity(Long id) {
        return directorRepository.findById(id);
    }
}