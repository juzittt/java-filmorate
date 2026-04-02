package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDbStorage userStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        UserMapper.validate(user);
        userStorage.saveUser(user);
        return user;
    }

    public User updateUser(User newUser) {
        UserMapper.validate(newUser);
        getUserById(newUser.getUserId());

        validateUniqueFields(newUser);

        userStorage.saveUser(newUser);
        return newUser;
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        getUserById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public void deleteUser(Long userId) {
        if (!userStorage.removeUser(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
    }

    private void validateUniqueFields(User user) {
        Optional<User> byEmail = userStorage.findByEmail(user.getEmail());
        if (byEmail.isPresent() && !byEmail.get().getUserId().equals(user.getUserId())) {
            throw new ValidationException("Email уже используется другим пользователем.");
        }

        Optional<User> byLogin = userStorage.findByLogin(user.getLogin());
        if (byLogin.isPresent() && !byLogin.get().getUserId().equals(user.getUserId())) {
            throw new ValidationException("Логин уже занят.");
        }
    }

    public User getUserById(Long userId) {
        return userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }
}