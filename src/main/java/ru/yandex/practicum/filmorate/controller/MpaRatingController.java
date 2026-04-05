package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.OK)
    public List<MpaDto> getAll() {
        return mpaService.getAllRatings();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public MpaDto getById(@PathVariable("id") Long id) {
        return mpaService.getRatingById(id);
    }
}