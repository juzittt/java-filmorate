package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review review) {
        if (review == null) return null;

        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .content(review.getContent())
                .isPositive(review.isPositive())
                .userId(review.getUserId())
                .filmId(review.getFilmId())
                .useful(review.getUseful())
                .build();
    }

    public Review toEntity(NewReviewRequest dto) {
        if (dto == null) return null;

        return Review.builder()
                .content(dto.getContent())
                .isPositive(dto.getIsPositive())
                .userId(dto.getUserId())
                .filmId(dto.getFilmId())
                .useful(dto.getUseful())
                .build();
    }
}
