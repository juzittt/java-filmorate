package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.DirectorDto;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {

    private final DirectorService directorService;

    @GetMapping
    public ResponseEntity<List<DirectorDto>> getDirectors() {
        return ResponseEntity.ok(directorService.getDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DirectorDto> getDirector(@PathVariable("id") Long id) {
        return ResponseEntity.ok(directorService.getDirectorById(id));
    }

    @PostMapping
    public ResponseEntity<DirectorDto> createDirector(@Valid @RequestBody DirectorDto request) {
        DirectorDto createdDirector = directorService.createDirector(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDirector);
    }

    @PutMapping
    public ResponseEntity<DirectorDto> updateDirector(@Valid @RequestBody DirectorDto request) {
        return ResponseEntity.ok(directorService.updateDirector(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDirector(@PathVariable("id") Long id) {
        directorService.deleteDirector(id);
        return ResponseEntity.noContent().build();
    }
}