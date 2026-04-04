package ru.yandex.practicum.filmorate.testdata;

import ru.yandex.practicum.filmorate.dto.*;
import ru.yandex.practicum.filmorate.model.*;

import java.time.LocalDate;
import java.util.List;

public class TestData {

    public static final String VALID_EMAIL = "user@yandex.ru";
    public static final String VALID_LOGIN = "validuser";
    public static final String VALID_NAME = "John Doe";
    public static final LocalDate VALID_BIRTHDAY = LocalDate.of(1990, 1, 1);

    public static final String VALID_FRIEND_EMAIL = "friend@yandex.ru";
    public static final String VALID_FRIEND_LOGIN = "validfriend";
    public static final String VALID_FRIEND_NAME = "John Wick";
    public static final LocalDate VALID_FRIEND_BIRTHDAY = LocalDate.of(2000, 1, 1);

    public static final String VALID_FILM_NAME = "Inception";
    public static final String VALID_FILM_DESCRIPTION = "A mind-bending thriller.";
    public static final LocalDate VALID_RELEASE_DATE = LocalDate.of(2010, 7, 16);
    public static final int VALID_DURATION = 148;

    public static final long MPA_ID = 1L;
    public static final String MPA_NAME = "G";

    public static final long GENRE_ID = 1L;
    public static final String GENRE_NAME = "Комедия";

    public static NewUserRequest createNewUserRequest() {
        return NewUserRequest.builder()
                .email(VALID_EMAIL)
                .login(VALID_LOGIN)
                .name(VALID_NAME)
                .birthday(VALID_BIRTHDAY)
                .build();
    }

    public static User createUser() {
        return User.builder()
                .email(VALID_EMAIL)
                .login(VALID_LOGIN)
                .name(VALID_NAME)
                .birthday(VALID_BIRTHDAY)
                .build();
    }

    public static User createFriend() {
        return User.builder()
                .email(VALID_FRIEND_EMAIL)
                .login(VALID_FRIEND_LOGIN)
                .name(VALID_FRIEND_NAME)
                .birthday(VALID_FRIEND_BIRTHDAY)
                .build();
    }

    public static NewFilmRequest createNewFilmRequest() {
        return NewFilmRequest.builder()
                .name(VALID_FILM_NAME)
                .description(VALID_FILM_DESCRIPTION)
                .releaseDate(VALID_RELEASE_DATE)
                .duration(VALID_DURATION)
                .mpa(createMpaDto())
                .genres(List.of(createGenreDto()))
                .build();
    }

    public static Film createFilm() {
        return Film.builder()
                .name(VALID_FILM_NAME)
                .description(VALID_FILM_DESCRIPTION)
                .releaseDate(VALID_RELEASE_DATE)
                .duration(VALID_DURATION)
                .mpa(new MpaRating(MPA_ID, MPA_NAME))
                .genres(List.of(new Genre(GENRE_ID, GENRE_NAME)))
                .build();
    }

    public static MpaDto createMpaDto() {
        return MpaDto.builder().id(MPA_ID).name(MPA_NAME).build();
    }

    public static GenreDto createGenreDto() {
        return GenreDto.builder().id(GENRE_ID).name(GENRE_NAME).build();
    }

    public static FilmDto createFilmDto(Long id) {
        return FilmDto.builder()
                .id(id)
                .name(VALID_FILM_NAME)
                .description(VALID_FILM_DESCRIPTION)
                .releaseDate(VALID_RELEASE_DATE)
                .duration(VALID_DURATION)
                .mpa(createMpaDto())
                .genres(List.of(createGenreDto()))
                .build();
    }

    public static UserDto createUserDto(Long userId){
        return UserDto.builder()
                .id(userId)
                .email(VALID_EMAIL)
                .login(VALID_LOGIN)
                .name(VALID_NAME)
                .birthday(VALID_BIRTHDAY)
                .build();
    }

    public static UpdateFilmRequest createUpdateFilmRequest(Long id) {
        return UpdateFilmRequest.builder()
                .id(id)
                .name(VALID_FILM_NAME)
                .description(VALID_FILM_DESCRIPTION)
                .releaseDate(VALID_RELEASE_DATE)
                .duration(VALID_DURATION)
                .mpa(createMpaDto())
                .genres(List.of(createGenreDto()))
                .build();

    }
}