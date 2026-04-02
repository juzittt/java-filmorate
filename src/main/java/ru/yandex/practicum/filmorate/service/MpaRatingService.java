package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingDbStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingDbStorage mpaStorage;

    public List<MpaRating> getAllRatings() {
        return mpaStorage.getAllRatings();
    }

    public MpaRating getRatingById(Long id) {
        return mpaStorage.getRatingById(id)
                .orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден."));
    }
}