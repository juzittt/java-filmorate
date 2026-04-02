package ru.yandex.practicum.filmorate.mapper;

import ru.yandex.practicum.filmorate.model.User;

public class UserMapper {
    public static void validate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}