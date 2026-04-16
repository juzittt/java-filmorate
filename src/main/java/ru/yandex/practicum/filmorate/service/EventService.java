package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventRepository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.mapper.EventMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    // Публичный метод для добавления события (вызывается из других сервисов)
    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        eventRepository.addEvent(userId, eventType, operation, entityId);
    }

    public List<EventDto> getFeed(Long userId) {
        return eventRepository.getFeed(userId).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());
    }
}