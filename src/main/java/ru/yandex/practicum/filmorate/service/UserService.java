package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dao.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final EventService eventService;
    private final FilmRepository filmRepository;

    public UserDto createUser(NewUserRequest request) {
        log.info("Creating new user: login='{}', email='{}'", request.getLogin(), request.getEmail());
        validateUniqueFields(request.getEmail(), request.getLogin(), null);

        User user = userMapper.toEntity(request);
        userRepository.save(user);
        log.debug("User saved with generated id={}", user.getUserId());

        UserDto result = userMapper.toDto(user);
        log.info("User created successfully with id={}", result.getId());
        return result;
    }

    public List<Film> getRecommendations(Long userId) {
        log.info("Fetching recommendations for user with id={}", userId);
        getUserEntityById(userId);

        List<Film> recommendations = filmRepository.getRecommendations(userId);
        log.info("Found {} recommended films for user {}", recommendations.size(), userId);
        return recommendations;
    }

    public UserDto updateUser(UpdateUserRequest request) {
        Long id = request.getId();
        log.info("Updating user with id={}", id);

        User user = getUserEntityById(id);
        validateUniqueFields(request.getEmail(), request.getLogin(), id);

        userMapper.updateUserFromRequest(user, request);
        log.debug("User fields updated from request");

        userRepository.update(user);
        log.debug("User changes saved to database");

        UserDto result = userMapper.toDto(user);
        log.info("User updated successfully: name='{}', login='{}'", result.getName(), result.getLogin());
        return result;
    }

    public UserDto getUserById(Long id) {
        log.info("Fetching user with id={}", id);
        UserDto dto = userMapper.toDto(getUserEntityById(id));
        log.debug("User found: name='{}', email='{}'", dto.getName(), dto.getEmail());
        return dto;
    }

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", id);
                    return new NotFoundException("Пользователь с id = " + id + " не найден.");
                });
    }

    public List<UserDto> getUsers() {
        log.info("Fetching all users");
        List<UserDto> users = userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} users", users.size());
        return users;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Adding friend: user={} -> friend={}", userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Cannot add self as friend: userId={}", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }

        userRepository.addFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.ADD, friendId);
        log.info("Friend added: {} is now friend of {}", friendId, userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Removing friend: user={} -> friend={}", userId, friendId);

        if (userId.equals(friendId)) {
            log.warn("Cannot remove self from friends: userId={}", userId);
            throw new ValidationException("Нельзя удалять самого себя из друзей.");
        }

        getUserEntityById(userId);
        getUserEntityById(friendId);

        userRepository.removeFriend(userId, friendId);
        eventService.addEvent(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info("Friend removed: {} is no longer friend of {}", friendId, userId);
    }

    public List<UserDto> getFriends(Long userId) {
        log.info("Fetching friends for user with id={}", userId);
        getUserEntityById(userId);

        List<UserDto> friends = userRepository.findFriends(userId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        log.info("User {} has {} friend(s)", userId, friends.size());
        return friends;
    }

    public List<UserDto> getCommonFriends(Long userId, Long otherId) {
        log.info("Fetching common friends between user {} and user {}", userId, otherId);

        List<UserDto> commonFriends = userRepository.findCommonFriends(userId, otherId).stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
        log.info("Found {} common friend(s)", commonFriends.size());
        return commonFriends;
    }

    public void deleteUser(Long id) {
        log.info("Deleting user with id={}", id);
        if (!userRepository.deleteById(id)) {
            log.warn("User deletion failed: user with id={} not found", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден.");
        }
        log.info("User with id={} deleted successfully", id);
    }

    private void validateUniqueFields(String email, String login, Long currentId) {
        log.debug("Validating unique fields: email='{}', login='{}', currentId={}", email, login, currentId);

        if (email != null && !email.isBlank()) {
            userRepository.findByEmail(email)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            log.warn("Email validation failed: email '{}' is already used by user {}", email, user.getUserId());
                            throw new ValidationException("Email уже используется другим пользователем.");
                        }
                    });
        }
        if (login != null && !login.isBlank()) {
            userRepository.findByLogin(login)
                    .ifPresent(user -> {
                        if (!user.getUserId().equals(currentId)) {
                            log.warn("Login validation failed: login '{}' is already taken by user {}", login, user.getUserId());
                            throw new ValidationException("Логин уже занят.");
                        }
                    });
        }
    }
}