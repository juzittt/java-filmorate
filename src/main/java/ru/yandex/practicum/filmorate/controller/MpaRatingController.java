package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {

    private final MpaRatingService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaDto>> getAll() {
        return ResponseEntity.ok(mpaService.getAllRatings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(mpaService.getRatingById(id));
    }
}