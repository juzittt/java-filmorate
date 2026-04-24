package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.*;
import ru.yandex.practicum.filmorate.dto.NewReviewRequest;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    @Mapping(target = "reviewId", source = "reviewId")
    ReviewDto toDto(Review review);

    @Mapping(target = "reviewId", ignore = true)
    Review toEntity(NewReviewRequest request);

    @Mapping(target = "reviewId", ignore = true)
    void updateReviewFromRequest(@MappingTarget Review review, NewReviewRequest request);
}