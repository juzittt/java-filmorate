package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository extends BaseRepository<User> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String FIND_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String FIND_BY_LOGIN_QUERY = "SELECT * FROM users WHERE login = ?";
    private static final String INSERT_QUERY = """
            INSERT INTO users (email, login, user_name, birthday)
            VALUES (?, ?, ?, ?)
            """;
    private static final String UPDATE_QUERY = """
            UPDATE users SET email = ?, login = ?, user_name = ?, birthday = ?
            WHERE user_id = ?
            """;
    private static final String FIND_FRIENDS_QUERY = """
            SELECT u.* FROM users u
            INNER JOIN friendship f ON u.user_id = f.friend_id
            WHERE f.user_id = ? AND f.status_id = 1
            """;
    private static final String FIND_COMMON_FRIENDS_QUERY = """
            SELECT u.* FROM users u
            INNER JOIN friendship f1 ON u.user_id = f1.friend_id
            INNER JOIN friendship f2 ON u.user_id = f2.friend_id
            WHERE f1.user_id = ? AND f2.user_id = ? AND f1.status_id = 1 AND f2.status_id = 1
            """;
    private static final String FIND_EXISTING_IDS_QUERY = """
            SELECT user_id FROM users WHERE user_id IN (%s)
            """;
    private static final String ADD_FRIEND_QUERY = """
            INSERT INTO friendship (user_id, friend_id, status_id) VALUES (?, ?, 1)
            """;
    private static final String REMOVE_FRIEND_QUERY = """
            DELETE FROM friendship WHERE user_id = ? AND friend_id = ?
            """;
    private static final String REMOVE_USER = "DELETE FROM users WHERE user_id = ?";

    public UserRepository(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    public Optional<User> findByEmail(String email) {
        return findOne(FIND_BY_EMAIL_QUERY, email);
    }

    public Optional<User> findByLogin(String login) {
        return findOne(FIND_BY_LOGIN_QUERY, login);
    }

    public User save(User user) {
        if (user.getUserId() == null) {
            long id = insert(INSERT_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    java.sql.Date.valueOf(user.getBirthday()));
            user.setUserId(id);
        } else {
            update(UPDATE_QUERY,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    java.sql.Date.valueOf(user.getBirthday()),
                    user.getUserId());
        }
        return user;
    }

    public List<User> findFriends(Long userId) {
        return findMany(FIND_FRIENDS_QUERY, userId);
    }

    public List<User> findCommonFriends(Long userId, Long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, userId, otherId);
    }

    public List<Long> findExistentIds(List<Long> ids) {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", "?".repeat(ids.size()));
        String sql = String.format(FIND_EXISTING_IDS_QUERY, placeholders);
        return jdbc.queryForList(sql, Long.class, ids.toArray());
    }

    public void addFriend(Long userId, Long friendId) {
        jdbc.update(ADD_FRIEND_QUERY, userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        jdbc.update(REMOVE_FRIEND_QUERY, userId, friendId);
    }

    public boolean removeUser(Long userId) {
        return delete(REMOVE_USER, userId);
    }
}