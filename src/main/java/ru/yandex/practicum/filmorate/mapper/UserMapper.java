package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Component
public class UserMapper {

    public User toEntity(NewUserRequest request) {
        return User.builder()
                .email(request.getEmail())
                .login(request.getLogin())
                .name(request.getName())
                .birthday(request.getBirthday())
                .build();
    }

    public UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getUserId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }

    public void updateUserFromRequest(User user, UpdateUserRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }
        if (request.getLogin() != null && !request.getLogin().isBlank()) {
            user.setLogin(request.getLogin());
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBirthday() != null) {
            user.setBirthday(request.getBirthday());
        }
    }
}