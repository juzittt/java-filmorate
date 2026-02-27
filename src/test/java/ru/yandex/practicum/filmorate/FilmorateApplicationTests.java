package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты Filmorate приложения")
class FilmorateApplicationTests {

	private FilmController filmController;
	private UserController userController;
	private Film validFilm;
	private User validUser;

	@BeforeEach
	void setUp() {
		filmController = new FilmController();
		userController = new UserController();

		validFilm = new Film();
		validFilm.setName("Тестовый фильм");
		validFilm.setDescription("Описание");
		validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
		validFilm.setDuration(120);

		validUser = new User();
		validUser.setEmail("test@example.com");
		validUser.setLogin("testlogin");
		validUser.setName("Тест Тестов");
		validUser.setBirthday(LocalDate.of(1990, 1, 1));
	}

	@Test
	@DisplayName("createFilm — валидный фильм — должен создаться успешно")
	void createFilm_validFilm_success() throws ValidationException {

		Film created = filmController.create(validFilm);

		assertNotNull(created.getId());
		assertEquals("Тестовый фильм", created.getName());
	}

	@Test
	@DisplayName("createFilm — название null — должен выбросить исключение")
	void createFilm_nameIsNull_throwsException() {
		validFilm.setName(null);

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("createFilm — название пустое — должен выбросить исключение")
	void createFilm_nameIsBlank_throwsException() {
		validFilm.setName("   ");

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("createFilm — описание длиной 201 — должен выбросить исключение")
	void createFilm_descriptionTooLong_throwsException() {
		validFilm.setDescription("a".repeat(201));

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("createFilm — описание ровно 200 символов — должен создаться успешно")
	void createFilm_descriptionExactly200_success() throws ValidationException {
		validFilm.setDescription("a".repeat(200));

		Film created = filmController.create(validFilm);

		assertEquals(200, created.getDescription().length());
	}

	@Test
	@DisplayName("createFilm — дата релиза до 28.12.1895 — должен выбросить исключение")
	void createFilm_releaseDateBeforeCinemaBirth_throwsException() {
		validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("createFilm — дата релиза 28.12.1895 — должен создаться успешно")
	void createFilm_releaseDateExactlyCinemaBirth_success() throws ValidationException {
		validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));

		Film created = filmController.create(validFilm);

		assertEquals(LocalDate.of(1895, 12, 28), created.getReleaseDate());
	}

	@Test
	@DisplayName("createFilm — длительность 0 — должен выбросить исключение")
	void createFilm_durationZero_throwsException() {
		validFilm.setDuration(0);

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("createFilm — отрицательная длительность — должен выбросить исключение")
	void createFilm_durationNegative_throwsException() {
		validFilm.setDuration(-10);

		assertThrows(ValidationException.class, () -> filmController.create(validFilm));
	}

	@Test
	@DisplayName("updateFilm — валидные данные — должен обновиться успешно")
	void updateFilm_validData_success() throws ValidationException {
		Film created = filmController.create(validFilm);
		Film updateData = new Film();
		updateData.setId(created.getId());
		updateData.setName("Обновлённый фильм");
		updateData.setDescription("Новое описание");
		updateData.setReleaseDate(LocalDate.of(2000, 1, 1));
		updateData.setDuration(150);

		Film updated = filmController.update(updateData);

		assertEquals("Обновлённый фильм", updated.getName());
		assertEquals("Новое описание", updated.getDescription());
		assertEquals(150, updated.getDuration());
	}

	@Test
	@DisplayName("updateFilm — id не существует — должен выбросить исключение")
	void updateFilm_idNotExists_throwsException() {
		Film updateData = new Film();
		updateData.setId(999L);
		updateData.setName("Фильм");

		assertThrows(ValidationException.class, () -> filmController.update(updateData));
	}

	@Test
	@DisplayName("createUser — валидный пользователь — должен создаться успешно")
	void createUser_validUser_success() throws ValidationException {

		User created = userController.create(validUser);

		assertNotNull(created.getId());
		assertEquals("test@example.com", created.getEmail());
	}

	@Test
	@DisplayName("createUser — email null — должен выбросить исключение")
	void createUser_emailNull_throwsException() {
		validUser.setEmail(null);

		assertThrows(ValidationException.class, () -> userController.create(validUser));
	}

	@Test
	@DisplayName("createUser — email пустой — должен выбросить исключение")
	void createUser_emailBlank_throwsException() {
		validUser.setEmail("   ");

		assertThrows(ValidationException.class, () -> userController.create(validUser));
	}

	@Test
	@DisplayName("createUser — email без @ — должен выбросить исключение")
	void createUser_emailWithoutAt_throwsException() {
		validUser.setEmail("testexample.com");

		assertThrows(ValidationException.class, () -> userController.create(validUser));
	}

	@Test
	@DisplayName("createUser — имя null — должно подставиться из логина")
	void createUser_nameNull_usesLogin() throws ValidationException {
		validUser.setName(null);

		User created = userController.create(validUser);

		assertEquals(validUser.getLogin(), created.getName());
	}

	@Test
	@DisplayName("createUser — имя пустое — должно подставиться из логина")
	void createUser_nameBlank_usesLogin() throws ValidationException {
		// Given
		validUser.setName("   ");

		User created = userController.create(validUser);

		assertEquals(validUser.getLogin(), created.getName());
	}

	@Test
	@DisplayName("createUser — дата рождения null — должен выбросить исключение")
	void createUser_birthdayNull_throwsException() {
		validUser.setBirthday(null);

		assertThrows(ValidationException.class, () -> userController.create(validUser));
	}

	@Test
	@DisplayName("createUser — дата рождения в будущем — должен выбросить исключение")
	void createUser_birthdayInFuture_throwsException() {
		validUser.setBirthday(LocalDate.now().plusDays(1));

		assertThrows(ValidationException.class, () -> userController.create(validUser));
	}

	@Test
	@DisplayName("updateUser — валидные данные — должен обновиться успешно")
	void updateUser_validData_success() throws ValidationException {
		User created = userController.create(validUser);
		User updateData = new User();
		updateData.setId(created.getId());
		updateData.setEmail("new@example.com");
		updateData.setLogin("newlogin");
		updateData.setName("Новое Имя");
		updateData.setBirthday(LocalDate.of(1995, 5, 5));

		User updated = userController.update(updateData);

		assertEquals("new@example.com", updated.getEmail());
		assertEquals("newlogin", updated.getLogin());
		assertEquals("Новое Имя", updated.getName());
		assertEquals(LocalDate.of(1995, 5, 5), updated.getBirthday());
	}

	@Test
	@DisplayName("updateUser — id не существует — должен выбросить исключение")
	void updateUser_idNotExists_throwsException() {
		User updateData = new User();
		updateData.setId(999L);
		updateData.setEmail("test@example.com");
		updateData.setLogin("login");
		updateData.setBirthday(LocalDate.of(1990, 1, 1));

		assertThrows(ValidationException.class, () -> userController.update(updateData));
	}
}