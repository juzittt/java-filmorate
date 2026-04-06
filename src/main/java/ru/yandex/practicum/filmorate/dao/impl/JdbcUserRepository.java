package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.dao.UserRepository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcUserRepository implements UserRepository {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = FIND_ALL + " WHERE user_id = ?";
    private static final String FIND_BY_EMAIL = FIND_ALL + " WHERE email = ?";
    private static final String FIND_BY_LOGIN = FIND_ALL + " WHERE login = ?";
    private static final String INSERT = "INSERT INTO users (email, login, user_name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ? WHERE user_id = ?";
    private static final String FIND_FRIENDS = """
        SELECT u.* FROM users u
        INNER JOIN friendship f ON u.user_id = f.friend_id
        WHERE f.user_id = ? AND f.status_id = 1
        """;
    private static final String FIND_COMMON_FRIENDS = """
        SELECT u.* FROM users u
        INNER JOIN friendship f1 ON u.user_id = f1.friend_id
        INNER JOIN friendship f2 ON u.user_id = f2.friend_id
        WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status_id = 1 AND f2.status_id = 1
        """;
    private static final String ADD_FRIEND = "INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, 1)";
    private static final String REMOVE_FRIEND = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
    private static final String DELETE_USER = "DELETE FROM users WHERE user_id = ?";

    @Override
    public List<User> findAll() {
        return jdbc.query(FIND_ALL, userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID, userRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_EMAIL, userRowMapper, email));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> findByLogin(String login) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_LOGIN, userRowMapper, login));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User save(User user) {
        if (user.getUserId() == null) {
            long id = insert(INSERT,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    Date.valueOf(user.getBirthday())
            );
            user.setUserId(id);
        } else {
            jdbc.update(UPDATE,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    Date.valueOf(user.getBirthday()),
                    user.getUserId());
        }
        return user;
    }

    @Override
    public void update(User user) {
        save(user);
    }

    @Override
    public List<User> findFriends(Long userId) {
        return jdbc.query(FIND_FRIENDS, userRowMapper, userId);
    }

    @Override
    public List<User> findCommonFriends(Long userId, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIENDS, userRowMapper, userId, otherId);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        validateUsersExist(userId, friendId);
        validateNoSelfFriendship(userId, friendId);
        validateNotAlreadyFriend(userId, friendId);

        jdbc.update(ADD_FRIEND, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND, userId, friendId);
    }

    @Override
    public boolean deleteById(Long id) {
        return jdbc.update(DELETE_USER, id) > 0;
    }

    private long insert(String sql, Object... params) {
        var keyHolder = new GeneratedKeyHolder();
        jdbc.update(conn -> {
            var ps = conn.prepareStatement(sql, new String[]{"user_id"});
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    private void validateUsersExist(Long userId, Long friendId) {
        if (!findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        if (!findById(friendId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + friendId + " не найден.");
        }
    }

    private void validateNoSelfFriendship(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья.");
        }
    }

    private void validateNotAlreadyFriend(Long userId, Long friendId) {
        List<User> friends = findFriends(userId);
        boolean already = friends.stream().anyMatch(f -> f.getUserId().equals(friendId));
        if (already) {
            throw new ValidationException("Пользователь уже в друзьях.");
        }
    }
}