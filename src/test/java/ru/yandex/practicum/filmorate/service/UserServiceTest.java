package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.testdata.TestData;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest
@DisplayName("UserService :: Business Logic Tests")
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    private NewUserRequest userReq;
    private User user;

    @BeforeEach
    void setUp() {
        userReq = TestData.createNewUserRequest();
        user = TestData.createUser();
    }

    @Test
    @DisplayName("addUser() should save user and assign ID")
    void addUser_ShouldSaveUser_AndAssignId() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setUserId(1L);
            return u;
        });

        var result = userService.createUser(userReq);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("user@yandex.ru");
    }

    @Test
    @DisplayName("addFriend() should link two users")
    void addFriend_ShouldLinkTwoUsers() {
        when(userRepository.findById(1L)).thenReturn(Optional.ofNullable(user));
        when(userRepository.findById(2L)).thenReturn(java.util.Optional.of(TestData.createFriend()));

        doNothing().when(userRepository).addFriend(1L, 2L);

        userService.addFriend(1L, 2L);

        verify(userRepository, times(1)).addFriend(1L, 2L);
    }

    @Test
    @DisplayName("addFriend() should throw ValidationException when self-friendship")
    void addFriend_ShouldThrowValidationException_WhenSelfFriendship() {
        assertThatThrownBy(() -> userService.addFriend(1L, 1L))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Нельзя добавить самого себя в друзья.");
    }
}