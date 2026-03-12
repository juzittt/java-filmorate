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
    public ResponseEntity<Film> addFilm(@Valid @RequestBody Film film) {
        Film added = filmService.addFilm(film);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @PutMapping
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody Film newFilm) {
        Film updated = filmService.updateFilm(newFilm);
        return ResponseEntity.ok(updated);
    }

    //! Не уверен, что тут правильно написал метод

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> deleteLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.deleteLike(id, userId);
        return ResponseEntity.noContent().build();
    }

    /*?Или все же оставить как было?
    ?@DeleteMapping("/{id}/like/{userId}")
    ?@ResponseStatus(HttpStatus.NO_CONTENT)
    ?public void deleteLike(@PathVariable Long id, @PathVariable Long userId) {
    ?    filmService.deleteLike(id, userId);
    ?}*/

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getMostPopularFilms(@RequestParam(required = false) Integer count) {
        List<Film> films = filmService.getMostPopularFilms(count);
        return ResponseEntity.ok(films);
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Set<Long>> getLikes(@PathVariable Long id) {
        Set<Long> likes = filmService.getLikes(id);
        return ResponseEntity.ok(likes);
    }
}