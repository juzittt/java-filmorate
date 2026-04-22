package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.dao.MpaRatingRepository;
import ru.yandex.practicum.filmorate.mapper.mapstruct.MpaRatingMapper;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MpaRatingService {

    private final MpaRatingRepository mpaRepository;
    private final MpaRatingMapper mpaMapper;

    public List<MpaDto> getAllRatings() {
        log.info("Fetching all MPA ratings");
        List<MpaDto> ratings = mpaRepository.findAll().stream()
                .map(mpaMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} MPA rating(s)", ratings.size());
        return ratings;
    }

    public MpaDto getRatingById(Long id) {
        log.info("Fetching MPA rating with id={}", id);
        return mpaRepository.findById(id)
                .map(rating -> {
                    log.debug("MPA rating found: id={}, name='{}'", rating.getRatingId(), rating.getName());
                    return mpaMapper.toDto(rating);
                })
                .orElseThrow(() -> {
                    log.warn("MPA rating with id={} not found", id);
                    return new NotFoundException("Рейтинг MPA с id = " + id + " не найден.");
                });
    }
}