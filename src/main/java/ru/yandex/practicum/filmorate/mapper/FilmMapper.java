package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

public class FilmMapper {
    public static void validate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate()
                .isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Самая ранняя разрешенная дата релиза — 28 декабря 1895 года.");
        }
    }
}