package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.FilmRepository;
import ru.yandex.practicum.filmorate.dao.UserRepository;
import ru.yandex.practicum.filmorate.dao.ReviewRepository;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.ReviewMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ReviewMapper reviewMapper;
    private final EventService eventService;
    private static final int DEFAULT_COUNT = 10;

    public ReviewDto createReview(NewReviewRequest request) {
        log.info("Creating new review for filmId={} by userId={}", request.getFilmId(), request.getUserId());
        validateNewReviewRequest(request);

        Review review = reviewMapper.toEntity(request);
        reviewRepository.save(review);
        log.debug("Review saved with id={}", review.getReviewId());

        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());

        ReviewDto result = reviewMapper.toDto(review);
        log.info("Review created successfully with id={}", result.getReviewId());
        return result;
    }

    public ReviewDto updateReview(UpdateReviewRequest request) {
        Long reviewId = request.getReviewId();
        log.info("Updating review with id={}", reviewId);

        Review existing = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Failed to update: review with id={} not found", reviewId);
                    return new NotFoundException("Отзыв с id = " + reviewId + " не найден");
                });

        existing.setContent(request.getContent());
        existing.setPositive(request.getIsPositive());

        reviewRepository.update(existing);
        eventService.addEvent(existing.getUserId(), EventType.REVIEW, Operation.UPDATE, existing.getReviewId());

        ReviewDto result = reviewMapper.toDto(existing);
        log.info("Review updated successfully");
        return result;
    }

    public void deleteReview(Long reviewId) {
        log.info("Deleting review with id={}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> {
                    log.warn("Failed to delete: review with id={} not found", reviewId);
                    return new NotFoundException("Отзыв не найден");
                });

        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
        reviewRepository.deleteById(reviewId);
        log.info("Review with id={} deleted successfully", reviewId);
    }

    public ReviewDto getReviewById(Long reviewId) {
        log.info("Fetching review with id={}", reviewId);
        return reviewRepository.findById(reviewId)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new NotFoundException(
                        "Отзыв с id = " + reviewId + " не найден"));
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        log.info("Fetching reviews for filmId={}, count={}", filmId, count);

        int limit = (count == null || count <= 0) ? DEFAULT_COUNT : count;
        return reviewRepository.findAllByFilmId(filmId, limit).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        log.info("User {} adding like to review {}", userId, reviewId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addLike(reviewId, userId);
        log.debug("Like added successfully");
    }

    public void addDislike(Long reviewId, Long userId) {
        log.info("User {} adding dislike to review {}", userId, reviewId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addDislike(reviewId, userId);
        log.debug("Dislike added successfully");
    }

    public void removeLike(Long reviewId, Long userId) {
        log.info("User {} removing like from review {}", userId, reviewId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.removeLike(reviewId, userId);
        log.debug("Like removed successfully");
    }

    public void removeDislike(Long reviewId, Long userId) {
        log.info("User {} removing dislike from review {}", userId, reviewId);
        validateReviewAndUser(reviewId, userId);
        reviewRepository.removeDislike(reviewId, userId);
        log.debug("Dislike removed successfully");
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        log.debug("Validating existence of review {} and user {}", reviewId, userId);

        if (!reviewRepository.findById(reviewId).isPresent()) {
            log.warn("Validation failed: review with id={} not found", reviewId);
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
        if (!userRepository.findById(userId).isPresent()) {
            log.warn("Validation failed: user with id={} not found", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateNewReviewRequest(NewReviewRequest request) {
        log.debug("Validating NewReviewRequest: userId={}, filmId={}, isPositive={}",
                request.getUserId(), request.getFilmId(), request.getIsPositive());

        if (request.getUserId() == null) {
            log.warn("Validation failed: userId is null");
            throw new ValidationException("userId не может быть пустым");
        }
        if (request.getFilmId() == null) {
            log.warn("Validation failed: filmId is null");
            throw new ValidationException("filmId не может быть пустым");
        }
        if (request.getIsPositive() == null) {
            log.warn("Validation failed: isPositive is null");
            throw new ValidationException("isPositive не может быть пустым");
        }

        if (!userRepository.findById(request.getUserId()).isPresent()) {
            log.warn("Validation failed: user with id={} does not exist", request.getUserId());
            throw new NotFoundException("Пользователь с id = " + request.getUserId() + " не найден");
        }
        if (!filmRepository.findById(request.getFilmId()).isPresent()) {
            log.warn("Validation failed: film with id={} does not exist", request.getFilmId());
            throw new NotFoundException("Фильм с id = " + request.getFilmId() + " не найден");
        }
    }
}