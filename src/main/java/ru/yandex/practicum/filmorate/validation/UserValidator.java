package ru.yandex.practicum.filmorate.validation;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

@UtilityClass
public class UserValidator {

    public void validate(User user) {
        if (user == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email не должен быть пустым или содержать только пробелы");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().trim().isEmpty()) {
            throw new ValidationException("Логин не должен быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}