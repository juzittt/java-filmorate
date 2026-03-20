package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User addUser(User user) {
        validate(user);

        Long id = (long) (getUsers().size() + 1);
        user.setUserId(id);
        userStorage.saveUser(user);
        log.info("User {} added with id: {}", user.getName(), user.getUserId());
        return user;
    }

    public User updateUser(User newUser) {
        validate(newUser);

        return userStorage.getUserById(newUser.getUserId())
                .map(user -> processUpdateUser(newUser))
                .orElseThrow(() -> {
                    log.error("User {} with id {} not found", newUser.getName(), newUser.getUserId());
                    return new NotFoundException("Пользователь " + newUser.getName() + " с id " + newUser.getUserId() + " не найден");
                });
    }

    public void addFriend(Long id, Long friendId) {
        checkUserPresence(id, friendId);
        checkUserConflict(id, friendId);

        log.info("User with id: {} added friend with id: {}", id, friendId);
        userStorage.addFriend(id, friendId);
    }

    public void removeFriend(Long id, Long friendId) {
        checkUserPresence(id, friendId);
        checkUserConflict(id, friendId);

        log.info("User with id: {} removed friend with id: {}", id, friendId);
        userStorage.removeFriend(id, friendId);
    }

    public List<User> getFriends(Long id) {
        User user = findUser(id);
        return userStorage.getFriends(user);
    }

    public List<User> getMutualFriends(Long id, Long otherId) {
        User user = findUser(id);
        User otherUser = findUser(otherId);

        List<User> userFriends = userStorage.getFriends(user);
        Set<User> otherUserFriends = new HashSet<>(userStorage.getFriends(otherUser));

        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .toList();
    }

    private User findUser(Long userId) {
        return userStorage.getUserById(userId).orElseThrow(() -> {
            log.error("User with id = {} not found.", userId);
            return new NotFoundException("Пользователь с id = " + userId + " не найден.");
        });
    }

    void checkUserPresence(Long... userIds) {
        Set<Long> userIdSet = new HashSet<>(Arrays.asList(userIds));
        List<Long> foundIds = userStorage.findExistentIds(userIdSet);

        if (userIdSet.size() != foundIds.size()) {
            String missingIds = userIdSet.stream()
                    .filter(ids -> !foundIds.contains(ids))
                    .collect(Collectors.toSet()).toString();
            log.error("Users not found with ids: {}", missingIds);
            throw new NotFoundException("Не найдены пользователи с id: " + missingIds);
        }
    }

    private void checkUserConflict(Long id, Long friendId) {
        if (Objects.equals(id, friendId)) {
            log.error("User with id = {} conflicted with friendId = {}.", id, friendId);
            throw new ValidationException(
                    "Попытка добавить или удалить друга с тем же id, что и у пользователя.");
        }
    }

    private void validate(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User processUpdateUser(User newUser) {
        userStorage.saveUser(newUser);
        log.info("Updated user with id: {}", newUser.getUserId());
        return newUser;
    }
}
