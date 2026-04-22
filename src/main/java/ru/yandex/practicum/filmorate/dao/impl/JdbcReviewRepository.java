package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.ReviewRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReviewRepository implements ReviewRepository {

    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewRowMapper;

    private static final String INSERT_REVIEW =
            "INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";

    private static final String FIND_REVIEW_BY_ID =
            """
            SELECT r.*, COALESCE(
                SUM(CASE WHEN rl.user_id IS NOT NULL THEN 1 ELSE 0 END) -
                SUM(CASE WHEN rd.user_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_likes rl ON r.review_id = rl.review_id
            LEFT JOIN review_dislikes rd ON r.review_id = rd.review_id
            WHERE r.review_id = ?
            GROUP BY r.review_id
            """;

    private static final String FIND_ALL_REVIEWS =
            """
            SELECT r.*, COALESCE(
                SUM(CASE WHEN rl.user_id IS NOT NULL THEN 1 ELSE 0 END) -
                SUM(CASE WHEN rd.user_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_likes rl ON r.review_id = rl.review_id
            LEFT JOIN review_dislikes rd ON r.review_id = rd.review_id
            GROUP BY r.review_id
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String FIND_ALL_REVIEWS_BY_FILM_ID =
            """
            SELECT r.*, COALESCE(
                SUM(CASE WHEN rl.user_id IS NOT NULL THEN 1 ELSE 0 END) -
                SUM(CASE WHEN rd.user_id IS NOT NULL THEN 1 ELSE 0 END), 0) AS useful
            FROM reviews r
            LEFT JOIN review_likes rl ON r.review_id = rl.review_id
            LEFT JOIN review_dislikes rd ON r.review_id = rd.review_id
            WHERE r.film_id = ?
            GROUP BY r.review_id
            ORDER BY useful DESC
            LIMIT ?
            """;

    private static final String UPDATE_REVIEW =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_REVIEW =
            "DELETE FROM reviews WHERE review_id = ?";

    private static final String ADD_LIKE =
            "MERGE INTO review_likes (review_id, user_id) KEY(review_id, user_id) VALUES (?, ?)";

    private static final String ADD_DISLIKE =
            "MERGE INTO review_dislikes (review_id, user_id) KEY(review_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String REMOVE_DISLIKE =
            "DELETE FROM review_dislikes WHERE review_id = ? AND user_id = ?";

    private static final String RECALCULATE_USEFUL =
            """
            UPDATE reviews
            SET useful = (
                    (SELECT COUNT(*) FROM review_likes   WHERE review_id = ?) -
                    (SELECT COUNT(*) FROM review_dislikes WHERE review_id = ?)
            )
            WHERE review_id = ?
            """;

    @Override
    public Review save(Review review) {
        Long id = insertWithGeneratedKey(INSERT_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());

        review.setReviewId(id);
        recalculateUseful(id);
        return findById(id).orElseThrow();
    }

    @Override
    public Optional<Review> findById(Long reviewId) {
        try {
            return Optional.ofNullable(jdbc.queryForObject(FIND_REVIEW_BY_ID, reviewRowMapper, reviewId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findAllByFilmId(Long filmId, int count) {
        if (filmId != null) {
            return jdbc.query(FIND_ALL_REVIEWS_BY_FILM_ID, reviewRowMapper, filmId, count);
        } else {
            return jdbc.query(FIND_ALL_REVIEWS, reviewRowMapper, count);
        }
    }

    @Override
    public void update(Review review) {
        int rows = jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        if (rows == 0) {
            throw new NotFoundException("Отзыв с id = " + review.getReviewId() + " не найден");
        }
        recalculateUseful(review.getReviewId());
    }

    @Override
    public void deleteById(Long reviewId) {
        int rows = jdbc.update(DELETE_REVIEW, reviewId);
        if (rows == 0) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
    }

    @Override
    public void addLike(Long reviewId, Long userId) {
        removeDislike(reviewId, userId);
        jdbc.update(ADD_LIKE, reviewId, userId);
        recalculateUseful(reviewId);
    }

    @Override
    public void addDislike(Long reviewId, Long userId) {
        removeLike(reviewId, userId);
        jdbc.update(ADD_DISLIKE, reviewId, userId);
        recalculateUseful(reviewId);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        jdbc.update(REMOVE_LIKE, reviewId, userId);
        recalculateUseful(reviewId);
    }

    @Override
    public void removeDislike(Long reviewId, Long userId) {
        jdbc.update(REMOVE_DISLIKE, reviewId, userId);
        recalculateUseful(reviewId);
    }

    private void recalculateUseful(Long reviewId) {
        jdbc.update(RECALCULATE_USEFUL, reviewId, reviewId, reviewId);
    }

    private Long insertWithGeneratedKey(String sql, Object... params) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"review_id"});
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }
}
