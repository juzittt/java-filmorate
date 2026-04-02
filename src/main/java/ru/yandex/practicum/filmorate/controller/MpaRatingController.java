package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {
    private final MpaRatingService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaRating>> getAll() {
        return ResponseEntity.ok(mpaService.getAllRatings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaRating> getById(@PathVariable Long id) {
        MpaRating rating = mpaService.getRatingById(id);
        return ResponseEntity.ok(rating);
    }
}