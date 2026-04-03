package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserDbStorage userStorage;
    private final UserMapper userMapper;

    public UserDto createUser(NewUserRequest request) {
        validateUniqueFields(request.getEmail(), request.getLogin(), null);
        User user = userMapper.toEntity(request);
        userStorage.saveUser(user);
        return userMapper.toDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        Long id = request.getId();
        User existing = getUserEntityById(id);

        validateUniqueFields(request.getEmail(), request.getLogin(), id);

        userMapper.updateEntityFromRequest(existing, request);
        userStorage.saveUser(existing);
        return userMapper.toDto(existing);
    }

    public List<UserDto> getUsers() {
        return userStorage.getUsers().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return userMapper.toDto(user);
    }

    public void addFriend(Long userId, Long friendId) {
        getUserEntityById(userId);
        User friend = getUserEntityById(friendId);
        userStorage.addFriend(userId, friend.getUserId());
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserEntityById(userId);
        getUserEntityById(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        getUserEntityById(userId);
        return userStorage.getFriends(userId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        if (!userStorage.removeUser(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }


    private User getUserEntityById(Long id) {
        return userStorage.getUserById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    private void validateUniqueFields(String email, String login, Long currentId) {
        if (email != null && !email.isBlank()) {
            userStorage.findByEmail(email)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            throw new ValidationException("Email уже используется.");
                        }
                    });
        }
        if (login != null && !login.isBlank()) {
            userStorage.findByLogin(login)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            throw new ValidationException("Логин уже занят.");
                        }
                    });
        }
    }
}