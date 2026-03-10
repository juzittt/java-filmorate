package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты UserController")
class UserControllerTest {

    private UserController userController;
    private Validator validator;

    @BeforeEach
    void setUp() {
        InMemoryUserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        userController = new UserController(userService);

        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        User defaultUser = User.builder()
                .email("test@email.com")
                .login("test-login")
                .name("test-name")
                .birthday(LocalDate.of(2000, 12, 12))
                .build();
        userController.addUser(defaultUser);
    }

    @Test
    @DisplayName("addUser — валидный пользователь — должен добавиться успешно")
    void addUser_validUser_success() {
        User validUser = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("user-name")
                .birthday(LocalDate.of(2002, 12, 12))
                .build();

        User added = userController.addUser(validUser);

        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty());
        assertNotNull(added.getId());
        assertEquals(2L, added.getId());
        assertEquals(validUser.getEmail(), added.getEmail());
        assertEquals(validUser.getLogin(), added.getLogin());
        assertEquals(validUser.getBirthday(), added.getBirthday());
        assertTrue(userController.getUsers().contains(added));
    }

    @Test
    @DisplayName("addUser — email пустой — должен выбросить исключение валидации")
    void addUser_emailBlank_validationError() {
        User invalidUser = User.builder()
                .email("")
                .login("login")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Email не должен быть пустым", message);
    }

    @Test
    @DisplayName("addUser — email null — должен выбросить исключение валидации")
    void addUser_emailNull_validationError() {
        User invalidUser = User.builder()
                .email(null)
                .login("login")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Email не должен быть пустым", message);
    }

    @Test
    @DisplayName("addUser — email без @ — должен выбросить исключение валидации")
    void addUser_emailInvalid_validationError() {
        User invalidUser = User.builder()
                .email("invalid-email")
                .login("login")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Email должен быть корректным", message);
    }

    @Test
    @DisplayName("addUser — логин пустой — должен выбросить исключение валидации")
    void addUser_loginBlank_validationError() {
        User invalidUser = User.builder()
                .email("user@email.com")
                .login("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Логин не должен быть пустым", message);
    }

    @Test
    @DisplayName("addUser — логин содержит пробелы — должен выбросить исключение валидации")
    void addUser_loginHasSpaces_validationError() {
        User invalidUser = User.builder()
                .email("user@email.com")
                .login("with space")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Логин не может содержать пробелы", message);
    }

    @Test
    @DisplayName("addUser — имя null — должно подставить логин")
    void addUser_nameNull_usesLogin() {
        User user = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name(null)
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User added = userController.addUser(user);

        assertEquals(user.getLogin(), added.getName());
    }

    @Test
    @DisplayName("addUser — имя пустое — должно подставить логин")
    void addUser_nameBlank_usesLogin() {
        User user = User.builder()
                .email("user@email.com")
                .login("user-login")
                .name("")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        User added = userController.addUser(user);

        assertEquals(user.getLogin(), added.getName());
    }

    @Test
    @DisplayName("addUser — дата рождения в будущем — должен выбросить исключение валидации")
    void addUser_birthdayInFuture_validationError() {
        User invalidUser = User.builder()
                .email("user@email.com")
                .login("login")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(invalidUser);
        assertFalse(violations.isEmpty());
        String message = violations.iterator().next().getMessage();
        assertEquals("Дата рождения не может быть в будущем", message);
    }

    @Test
    @DisplayName("updateUser — валидные данные, пользователь найден — должен обновиться")
    void updateUser_validData_userFound_success() {
        User updatedUser = User.builder()
                .id(1L)
                .email("updated@email.com")
                .login("updated-login")
                .name("updated-name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User result = userController.updateUser(updatedUser);

        assertNotNull(result);
        assertEquals(updatedUser.getEmail(), result.getEmail());
        assertEquals(updatedUser.getName(), result.getName());
    }

    @Test
    @DisplayName("updateUser — пользователь не найден — должен выбросить NotFoundException")
    void updateUser_userNotFound_throwNotFoundException() {
        User missingUser = User.builder()
                .id(999L)
                .email("missing@email.com")
                .login("login")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.updateUser(missingUser)
        );
        assertEquals("Пользователь login с id 999 не найден", exception.getMessage());
    }

    @Test
    @DisplayName("addFriend — валидные id — должен добавить друга")
    void addFriend_validIds_friendAdded() {
        User friend = User.builder()
                .email("friend@email.com")
                .login("friend-login")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();
        userController.addUser(friend);

        userController.addFriend(1L, friend.getId());

        List<User> friends = userController.getFriends(1L);
        List<User> friendOfFriend = userController.getFriends(friend.getId());
        assertTrue(friends.contains(friend));
        assertTrue(friendOfFriend.stream().anyMatch(u -> u.getId().equals(1L)));
    }

    @Test
    @DisplayName("addFriend — id совпадают — должен выбросить ValidationException")
    void addFriend_idsEqual_throwValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.addFriend(1L, 1L)
        );
        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
    }

    @Test
    @DisplayName("deleteFriend — id совпадают — должен выбросить ValidationException")
    void deleteFriend_idsEqual_throwValidationException() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.deleteFriend(1L, 1L)
        );
        assertEquals("Попытка добавить или удалить друга с тем же id, что и у пользователя.", exception.getMessage());
    }

    @Test
    @DisplayName("addFriend — один из id не найден — должен выбросить NotFoundException")
    void addFriend_idNotFound_throwNotFoundException() {
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userController.addFriend(1L, 999L)
        );
        assertEquals("Не найдены пользователи с id: [999]", exception.getMessage());
    }

    @Test
    @DisplayName("getCommonFriends — есть общие друзья — должен вернуть список")
    void getCommonFriends_withMutual_returnsList() {
        setUpUserFriends();

        List<User> common = userController.getCommonFriends(1L, 4L);

        assertEquals(1, common.size());
        assertEquals(3L, common.get(0).getId());
    }

    void setUpUserFriends() {
        User user2 = User.builder().email("2@e").login("l2").birthday(LocalDate.now()).build();
        User user3 = User.builder().email("3@e").login("l3").birthday(LocalDate.now()).build();
        User user4 = User.builder().email("4@e").login("l4").birthday(LocalDate.now()).build();

        userController.addUser(user2);
        userController.addUser(user3);
        userController.addUser(user4);

        userController.addFriend(1L, user2.getId());
        userController.addFriend(1L, user3.getId());
        userController.addFriend(1L, user4.getId());

        userController.addFriend(user4.getId(), user3.getId());
    }
}