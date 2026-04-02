package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@ActiveProfiles("test")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("UserController тесты")
public class UserControllerTest {

    private static final Logger log = LoggerFactory.getLogger(UserControllerTest.class);

    private final UserController userController;

    private Validator validator;
    private User createdUser;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }

        User baseUser = User.builder()
                .email("base@email.com")
                .login("Базовый")
                .birthday(LocalDate.of(2000, 1, 1))
                .build();

        ResponseEntity<User> response = userController.createUser(baseUser);
        createdUser = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        log.info("Подготовлен пользователь: {}", createdUser.getLogin());
    }

    @Test
    @DisplayName("Должен создать пользователя, если данные валидны — возвращает 201 OK")
    void shouldCreateUserWhenValid() {
        User user = User.builder()
                .email("valid@email.com")
                .login("validlogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1985, 5, 5))
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        ResponseEntity<User> response = userController.createUser(user);
        User saved = response.getBody();

        assertThat(violations).isEmpty();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(saved).isNotNull();
        assertThat(saved.getUserId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("valid@email.com");
        assertThat(saved.getLogin()).isEqualTo("validlogin");
    }


    @Test
    @DisplayName("Должен выбросить исключение при попытке добавить друга самому себе")
    void shouldNotAllowSelfFriendship() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> userController.addFriend(createdUser.getUserId(), createdUser.getUserId())
        );

        assertThat(exception.getMessage()).isEqualTo("Нельзя добавить самого себя в друзья.");
    }

    @Test
    @DisplayName("Должен получить список друзей — возвращает 200 OK")
    void shouldGetFriendsList() {
        User friend = User.builder()
                .email("friend@e.com")
                .login("friend")
                .birthday(LocalDate.now())
                .build();

        ResponseEntity<User> friendResponse = userController.createUser(friend);
        User savedFriend = friendResponse.getBody();

        userController.addFriend(createdUser.getUserId(), savedFriend.getUserId());

        ResponseEntity<List<User>> friendsResponse = userController.getFriends(createdUser.getUserId());
        List<User> friends = friendsResponse.getBody();

        assertThat(friendsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getUserId()).isEqualTo(savedFriend.getUserId());
    }
}