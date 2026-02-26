package ru.yandex.practicum.filmorate.validation;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

@UtilityClass
public class FilmValidator {

    private static final LocalDate CREATION_DATE = LocalDate.of(1895, 12, 28);

    public void validate(Film film) {
        if (film == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        if (film.getName() == null || film.getName().trim().isEmpty()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Длина описания не должна быть больше 200");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CREATION_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}