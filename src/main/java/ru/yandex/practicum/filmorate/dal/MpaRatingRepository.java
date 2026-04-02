package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaRatingRepository extends BaseRepository<MpaRating> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_rating";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_rating WHERE rating_id = ?";

    public MpaRatingRepository(JdbcTemplate jdbc, MpaRatingRowMapper mapper) {
        super(jdbc, mapper);
    }

    public List<MpaRating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<MpaRating> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}