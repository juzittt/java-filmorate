package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.dao.MpaRatingRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcMpaRatingRepository implements MpaRatingRepository {

    private final JdbcTemplate jdbc;

    private static final String FIND_ALL = "SELECT * FROM mpa_rating ORDER BY rating_id";
    private static final String FIND_BY_ID = "SELECT * FROM mpa_rating WHERE rating_id = ?";

    @Override
    public List<MpaRating> findAll() {
        return jdbc.query(FIND_ALL, new MpaRatingRowMapper());
    }

    @Override
    public Optional<MpaRating> findById(Long id) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_BY_ID, new MpaRatingRowMapper(), id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}