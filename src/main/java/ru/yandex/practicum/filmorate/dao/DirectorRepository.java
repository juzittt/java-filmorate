package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

public interface DirectorRepository {
    List<Director> findAll();

    Director findById(Long id);

    Director create(Director director);

    Director update(Director director);

    void deleteById(Long id);

    boolean existsById(Long id);
}
