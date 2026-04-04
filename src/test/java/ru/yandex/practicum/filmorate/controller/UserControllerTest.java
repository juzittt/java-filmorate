package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.testdata.TestData;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("UserController :: HTTP Contract Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private NewUserRequest user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = TestData.createNewUserRequest();
        userDto = TestData.createUserDto(1L);
    }

    @Test
    @DisplayName("createUser() should return 201 when valid data")
    void createUser_ShouldReturn201_WhenValidData() throws Exception {
        UserDto dto = userDto;

        when(userService.createUser(any(NewUserRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(TestData.VALID_EMAIL))
                .andExpect(jsonPath("$.login").value(TestData.VALID_LOGIN))
                .andExpect(jsonPath("$.name").value(TestData.VALID_NAME));

        verify(userService, times(1)).createUser(any(NewUserRequest.class));
    }

    @Test
    @DisplayName("getUserById() should return 200 when user exists")
    void getUserById_ShouldReturn200_WhenUserExists() throws Exception {
        UserDto dto = userDto;

        when(userService.getUserById(1L)).thenReturn(dto);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value(TestData.VALID_EMAIL))
                .andExpect(jsonPath("$.name").value(TestData.VALID_NAME));
    }

    @Test
    @DisplayName("getFriends() should return 200 with empty list when no friends")
    void getFriends_ShouldReturn200_WithEmptyList() throws Exception {
        when(userService.getFriends(1L)).thenReturn(List.of());

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("addFriend() should return 204 when valid")
    void addFriend_ShouldReturn204_WhenValid() throws Exception {
        doNothing().when(userService).addFriend(1L, 2L);

        mockMvc.perform(put("/users/1/friends/2"))
                .andExpect(status().isNoContent());

        verify(userService).addFriend(1L, 2L);
    }

    @Test
    @DisplayName("removeFriend() should return 204 when valid")
    void removeFriend_ShouldReturn204_WhenValid() throws Exception {
        doNothing().when(userService).removeFriend(1L, 2L);

        mockMvc.perform(delete("/users/1/friends/2"))
                .andExpect(status().isNoContent());

        verify(userService).removeFriend(1L, 2L);
    }

    @Test
    @DisplayName("addFriend() should throw ValidationException when self-friendship")
    void addFriend_ShouldThrowValidationException_WhenSelfFriendship() throws Exception {
        doThrow(new ru.yandex.practicum.filmorate.exception.ValidationException("Нельзя добавить самого себя в друзья."))
                .when(userService).addFriend(1L, 1L);

        mockMvc.perform(put("/users/1/friends/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Нельзя добавить самого себя в друзья."));
    }
}