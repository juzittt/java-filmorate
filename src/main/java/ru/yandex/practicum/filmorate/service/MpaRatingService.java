package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MpaRatingService {

    private final MpaRatingDbStorage mpaStorage;
    private final FilmMapper filmMapper;

    public List<MpaDto> getAllRatings() {
        return mpaStorage.getAllRatings().stream()
                .map(filmMapper::toDto)
                .collect(Collectors.toList());
    }

    public MpaDto getRatingById(Long id) {
        return mpaStorage.getRatingById(id)
                .map(filmMapper::toDto)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден."));
    }
}