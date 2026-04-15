package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final ReviewMapper reviewMapper;
    private final EventService eventService;

    public ReviewDto createReview(NewReviewRequest request) {
        validateNewReviewRequest(request);

        Review review = reviewMapper.toEntity(request);
        reviewRepository.save(review);
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.ADD, review.getReviewId());
        return reviewMapper.toDto(review);
    }

    public ReviewDto updateReview(UpdateReviewRequest request) {
        Review existing = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new NotFoundException(
                        "Отзыв с id = " + request.getReviewId() + " не найден"));

        existing.setContent(request.getContent());
        existing.setPositive(request.getIsPositive());

        reviewRepository.update(existing);
        eventService.addEvent(existing.getUserId(), EventType.REVIEW, Operation.UPDATE, existing.getReviewId());
        return reviewMapper.toDto(existing);
    }

    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        eventService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, reviewId);
        reviewRepository.deleteById(reviewId);
    }

    public ReviewDto getReviewById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .map(reviewMapper::toDto)
                .orElseThrow(() -> new NotFoundException(
                        "Отзыв с id = " + reviewId + " не найден"));
    }

    public List<ReviewDto> getReviews(Long filmId, Integer count) {
        int limit = (count == null || count <= 0) ? 10 : count;
        return reviewRepository.findAllByFilmId(filmId, limit).stream()
                .map(reviewMapper::toDto)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addLike(reviewId, userId);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewRepository.addDislike(reviewId, userId);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewRepository.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewRepository.removeDislike(reviewId, userId);
    }

    private void validateReviewAndUser(Long reviewId, Long userId) {
        if (!reviewRepository.findById(reviewId).isPresent()) {
            throw new NotFoundException("Отзыв с id = " + reviewId + " не найден");
        }
        if (!userRepository.findById(userId).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    private void validateNewReviewRequest(NewReviewRequest request) {
        if (request.getUserId() == null) {
            throw new ValidationException("userId не может быть пустым");
        }
        if (request.getFilmId() == null) {
            throw new ValidationException("filmId не может быть пустым");
        }
        if (request.getIsPositive() == null) {
            throw new ValidationException("isPositive не может быть пустым");
        }

        if (!userRepository.findById(request.getUserId()).isPresent()) {
            throw new NotFoundException("Пользователь с id = " + request.getUserId() + " не найден");
        }
        if (!filmRepository.findById(request.getFilmId()).isPresent()) {
            throw new NotFoundException("Фильм с id = " + request.getFilmId() + " не найден");
        }
    }
}