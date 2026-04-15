package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventService eventService;

    public UserDto createUser(NewUserRequest request) {
        validateUniqueFields(request.getEmail(), request.getLogin(), null);
        User user = userMapper.toEntity(request);
        userRepository.save(user);
        return userMapper.toDto(user);
    }

    public UserDto updateUser(UpdateUserRequest request) {
        Long id = request.getId();
        User user = getUserEntityById(id);
        validateUniqueFields(request.getEmail(), request.getLogin(), id);

        userMapper.updateUserFromRequest(user, request);
        userRepository.update(user);
        return userMapper.toDto(user);
    }

    public UserDto getUserById(Long id) {
        return userMapper.toDto(getUserEntityById(id));
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    public List<UserDto> getUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }
        userRepository.addFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя удалять самого себя из друзей.");
        }

        getUserEntityById(userId);
        getUserEntityById(friendId);

        userRepository.removeFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
    }

    public List<UserDto> getFriends(Long userId) {
        getUserEntityById(userId);
        return userRepository.findFriends(userId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) {
        return userRepository.findCommonFriends(userId, otherId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        if (!userRepository.deleteById(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
    }

    private void validateUniqueFields(String email, String login, Long currentId) {
        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            throw new ValidationException("Email уже используется другим пользователем.");
                        }
                    });
        }
        if (login != null && !login.isBlank()) {
            userRepository.findByLogin(login)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            throw new ValidationException("Логин уже занят.");
                        }
                    });
        }
    }
}