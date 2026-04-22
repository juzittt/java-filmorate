package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.dao.GenreRepository;
import ru.yandex.practicum.filmorate.mapper.mapstruct.GenreMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;

    public List<GenreDto> getAllGenres() {
        log.info("Fetching all genres");
        List<GenreDto> genres = genreRepository.findAll().stream()
                .map(genreMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} genres", genres.size());
        return genres;
    }

    public GenreDto getGenreById(Long id) {
        log.info("Fetching genre with id={}", id);

        return genreRepository.findById(id)
                .map(genre -> {
                    log.debug("Genre found: id={}, name='{}'", genre.getGenreId(), genre.getName());
                    return genreMapper.toDto(genre);
                })
                .orElseThrow(() -> {
                    log.warn("Genre with id={} not found", id);
                    return new NotFoundException("Жанр с id = " + id + " не найден.");
                });
    }
}