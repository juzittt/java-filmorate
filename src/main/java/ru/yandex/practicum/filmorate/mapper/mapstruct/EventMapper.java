package ru.yandex.practicum.filmorate.mapper.mapstruct;

import org.mapstruct.Mapper;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.model.Event;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto toDto(Event event);
}