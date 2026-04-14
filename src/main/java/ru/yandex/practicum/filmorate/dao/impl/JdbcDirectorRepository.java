package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.DirectorRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    private static final String FIND_ALL = """
            SELECT director_id, name
            FROM directors
            ORDER BY director_id
            """;

    private static final String FIND_BY_ID = """
            SELECT director_id, name
            FROM directors
            WHERE director_id = ?
            """;

    private static final String CREATE = """
            INSERT INTO directors (name)
            VALUES (?)
            """;

    private static final String UPDATE = """
            UPDATE directors
            SET name = ?
            WHERE director_id = ?
            """;

    private static final String DELETE_BY_ID = """
            DELETE FROM directors
            WHERE director_id = ?
            """;

    private static final String EXISTS_BY_ID = """
            SELECT COUNT(*)
            FROM directors
            WHERE director_id = ?
            """;

    @Override
    public List<Director> findAll() {
        return jdbc.query(FIND_ALL, directorRowMapper);
    }

    @Override
    public Director findById(Long id) {
        List<Director> directors = jdbc.query(FIND_BY_ID, directorRowMapper, id);

        if (directors.isEmpty()) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }

        return directors.get(0);
    }

    @Override
    public Director create(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(CREATE, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setDirectorId(keyHolder.getKey().longValue());
        return director;
    }

    @Override
    public Director update(Director director) {
        if (!existsById(director.getDirectorId())) {
            throw new NotFoundException("Режиссёр с id=" + director.getDirectorId() + " не найден");
        }

        jdbc.update(UPDATE, director.getName(), director.getDirectorId());
        return director;
    }

    @Override
    public void deleteById(Long id) {
        if (!existsById(id)) {
            throw new NotFoundException("Режиссёр с id=" + id + " не найден");
        }

        jdbc.update(DELETE_BY_ID, id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer count = jdbc.queryForObject(EXISTS_BY_ID, Integer.class, id);
        return count != null && count > 0;
    }
}