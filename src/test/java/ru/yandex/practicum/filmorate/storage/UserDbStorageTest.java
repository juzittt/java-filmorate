package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("UserDbStorage тесты")
public class UserDbStorageTest {

    private static final Logger log = LoggerFactory.getLogger(UserDbStorageTest.class);

    private final UserDbStorage storage;

    @Test
    @DisplayName("Должен сохранить пользователя — getUserById возвращает его")
    void shouldSaveUser() {
        User user = User.builder()
                .name("Save")
                .email("user@save.com")
                .login("saver")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        storage.saveUser(user);
        Optional<User> saved = storage.getUserById(user.getUserId());

        assertThat(saved).isPresent();
        assertThat(saved.get().getEmail()).isEqualTo("user@save.com");
        log.info("Сохранён пользователь: {}", saved.get().getLogin());
    }

    @Test
    @DisplayName("Должен обновить пользователя — изменения сохранены")
    void shouldUpdateUser() {
        User user = User.builder()
                .name("Old Name")
                .email("old@email.com")
                .login("oldlogin")
                .birthday(LocalDate.of(1980, 1, 1))
                .build();
        storage.saveUser(user);

        User updated = user.toBuilder().name("New Name").login("newlogin").build();
        storage.saveUser(updated);
        Optional<User> result = storage.getUserById(user.getUserId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("New Name");
        assertThat(result.get().getLogin()).isEqualTo("newlogin");
    }

    @Test
    @DisplayName("Должен добавить друга — getFriends возвращает второго")
    void shouldAddFriend() {
        User u1 = createUser("u1@e.com", "user1");
        User u2 = createUser("u2@e.com", "user2");

        storage.addFriend(u1.getUserId(), u2.getUserId());
        List<User> friends = storage.getFriends(u1.getUserId());

        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getUserId()).isEqualTo(u2.getUserId());
    }

    @Test
    @DisplayName("Должен получить общих друзей — возвращает список")
    void shouldGetCommonFriends() {
        User u1 = createUser("u1@e.com", "user1");
        User u2 = createUser("u2@e.com", "user2");
        User common = createUser("common@e.com", "common");

        storage.addFriend(u1.getUserId(), common.getUserId());
        storage.addFriend(u2.getUserId(), common.getUserId());

        List<User> commonFriends = storage.getCommonFriends(u1.getUserId(), u2.getUserId());

        assertThat(commonFriends).hasSize(1);
        assertThat(commonFriends.get(0).getUserId()).isEqualTo(common.getUserId());
    }

    @Test
    @DisplayName("Не должен позволять добавить самого себя в друзья")
    void shouldNotAllowSelfFriendship() {
        User user = createUser("self@e.com", "self");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> storage.addFriend(user.getUserId(), user.getUserId())
        );
        assertThat(exception.getMessage()).isEqualTo("Нельзя добавить самого себя в друзья.");
    }

    private User createUser(String email, String login) {
        User user = User.builder()
                .name(login)
                .email(email)
                .login(login)
                .birthday(LocalDate.now())
                .build();
        storage.saveUser(user);
        return user;
    }
}