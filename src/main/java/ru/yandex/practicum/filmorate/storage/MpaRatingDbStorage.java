package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.MpaRatingRepository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MpaRatingDbStorage {
    private final MpaRatingRepository mpaRatingRepository;

    public List<MpaRating> getAllRatings() {
        return mpaRatingRepository.findAll();
    }

    public Optional<MpaRating> getRatingById(Long id) {
        return mpaRatingRepository.findById(id);
    }
}