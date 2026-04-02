package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public ResponseEntity<List<Film>> getFilms() {
        return ResponseEntity.ok(filmService.getFilms());
    }

    @PostMapping
    public ResponseEntity<Film> createFilm(@Valid @RequestBody Film film) {
        Film created = filmService.addFilm(film);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film film) {
        Film updated = filmService.updateFilm(film);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopular(@RequestParam(defaultValue = "10") Integer count) {
        List<Film> films = filmService.getMostPopularFilms(count);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Set<Long>> getLikes(@PathVariable Long id) {
        Set<Long> likes = filmService.getLikes(id);
        return ResponseEntity.ok(likes);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getFilm(@PathVariable Long id) {
        return filmService.getFilmById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFilm(@PathVariable Long id) {
        filmService.deleteFilm(id);
        return ResponseEntity.noContent().build();
    }
}