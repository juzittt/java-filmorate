package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public ResponseEntity<FilmDto> createFilm(@Valid @RequestBody NewFilmRequest request) {
        FilmDto createdFilm = filmService.createFilm(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFilm);
    }

    @PutMapping
    public ResponseEntity<FilmDto> updateFilm(@Valid @RequestBody UpdateFilmRequest request) {
        return ResponseEntity.ok(filmService.updateFilm(request));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilms() {
        return ResponseEntity.ok(filmService.getFilms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FilmDto> getFilm(@PathVariable("id") Long id) {
        return ResponseEntity.ok(filmService.getFilmById(id));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<FilmDto>> getPopular(
            @RequestParam(defaultValue = "100") Integer count,
            @RequestParam(required = false) Long genreId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(filmService.getPopularFilms(count, genreId, year));
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Set<Long>> getLikes(@PathVariable("id") Long id) {
        return ResponseEntity.ok(filmService.getLikes(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable("id") Long id) {
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/director/{directorId}")
    public ResponseEntity<List<FilmDto>> getFilmsByDirector(@PathVariable("directorId") Long directorId,
                                            @RequestParam("sortBy") String sortBy) {
        return ResponseEntity.ok(filmService.getFilmsByDirector(directorId, sortBy));
    }

    @GetMapping("/common")
    public ResponseEntity<List<FilmDto>> getCommonFilms(@RequestParam Long userId,
                                        @RequestParam Long friendId) {
        return ResponseEntity.ok(filmService.getCommonFilms(userId, friendId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<FilmDto>> search(
            @RequestParam(name = "query") String query,
            @RequestParam(name = "by") String by
    ) {
        return ResponseEntity.ok(filmService.searchFilms(query, by));
    }
}