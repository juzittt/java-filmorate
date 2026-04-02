package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final UserRepository userRepository;

    @Override
    public void saveUser(User user) {
        if (user.getUserId() == null) {
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new ValidationException("Email уже используется.");
            }
            if (userRepository.findByLogin(user.getLogin()).isPresent()) {
                throw new ValidationException("Логин уже занят.");
            }
        }
        userRepository.save(user);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);
        validateNoSelfFriendship(userId, friendId);
        validateNotAlreadyFriend(userId, friendId);

        userRepository.addFriend(userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        userRepository.removeFriend(userId, friendId);
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getFriends(Long userId) {
        return userRepository.findFriends(userId);
    }

    public List<User> getCommonFriends(Long id, Long otherId) {
        return userRepository.findCommonFriends(id, otherId);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    private void validateUsersExist(Long userId, Long friendId) {
        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (!userRepository.findById(friendId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
    }

    private void validateNoSelfFriendship(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }
    }

    private void validateNotAlreadyFriend(Long userId, Long friendId) {
        List<User> friends = userRepository.findFriends(userId);
        boolean alreadyFriend = friends.stream().anyMatch(f -> f.getUserId().equals(friendId));
        if (alreadyFriend) {
            throw new ValidationException("Пользователь уже в друзьях.");
        }
    }

    public boolean removeUser(Long userId) {
        return userRepository.removeUser(userId);
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}