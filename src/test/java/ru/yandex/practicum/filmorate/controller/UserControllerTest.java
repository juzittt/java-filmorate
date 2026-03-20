package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@DisplayName("Интеграционные тесты UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .login("test-login")
                .name("Test Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    @DisplayName("GET /users — должен вернуть список пользователей")
    void getUsers_shouldReturnUserList() throws Exception {
        when(userService.getUsers()).thenReturn(List.of(validUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService).getUsers();
    }

    @Test
    @DisplayName("POST /users — валидный пользователь — должен вернуть 201")
    void addUser_validUser_shouldReturnCreated() throws Exception {
        when(userService.addUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Name"));

        verify(userService).addUser(any(User.class));
    }

    @Test
    @DisplayName("POST /users — пустой email — должен вернуть 400")
    void addUser_emailBlank_shouldReturnBadRequest() throws Exception {
        User invalidUser = validUser.toBuilder().email("").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("Email не должен быть пустым"))
                .andExpect(jsonPath("$.email").exists());
    }

    @Test
    @DisplayName("PUT /users — обновление пользователя — должен вернуть 200")
    void updateUser_validData_shouldReturnOk() throws Exception {
        when(userService.updateUser(any(User.class))).thenReturn(validUser);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Name"));

        verify(userService).updateUser(any(User.class));
    }

    @Test
    @DisplayName("PUT /users — пользователь не найден — должен вернуть 404")
    void updateUser_notFound_shouldReturnNotFound() throws Exception {
        when(userService.updateUser(any(User.class)))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
    }

    @Test
    @DisplayName("PUT /users/{id}/friends/{friendId} — добавление друга — должен вернуть 204")
    void addFriend_validIds_shouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isNoContent());

        verify(userService).addFriend(1L, 2L);
    }

    @Test
    @DisplayName("DELETE /users/{id}/friends/{friendId} — удаление друга — должен вернуть 204")
    void deleteFriend_validIds_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isNoContent());

        verify(userService).removeFriend(1L, 2L);
    }

    @Test
    @DisplayName("GET /users/{id}/friends — должен вернуть список друзей")
    void getFriends_shouldReturnFriends() throws Exception {
        when(userService.getFriends(1L)).thenReturn(List.of(validUser));

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(userService).getFriends(1L);
    }

    @Test
    @DisplayName("GET /users/{id}/friends/common/{otherId} — должен вернуть общих друзей")
    void getCommonFriends_shouldReturnMutual() throws Exception {
        when(userService.getMutualFriends(1L, 2L)).thenReturn(List.of(validUser));

        mockMvc.perform(get("/users/1/friends/common/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(userService).getMutualFriends(1L, 2L);
    }
}