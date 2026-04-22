package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

@Mapper(componentModel = "spring")
public interface MpaRatingMapper {
    @Mapping(target = "ratingId", source = "id")
    MpaRating toEntity(MpaDto dto);

    @InheritInverseConfiguration
    MpaDto toDto(MpaRating rating);
}
