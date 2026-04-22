package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dao.EventRepository;
import ru.yandex.practicum.filmorate.dto.EventDto;
import ru.yandex.practicum.filmorate.mapper.mapstruct.EventMapper;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        log.debug("Adding event: user={}, type={}, operation={}, entityId={}",
                userId, eventType, operation, entityId);

        validateUserId(userId);
        validateEventType(eventType);
        validateOperation(operation);

        eventRepository.addEvent(userId, eventType, operation, entityId);
        log.debug("Event successfully added to repository");
    }

    public List<EventDto> getFeed(Long userId) {
        log.info("Fetching event feed for user with id={}", userId);

        validateUserId(userId);

        List<EventDto> events = eventRepository.getFeed(userId).stream()
                .map(eventMapper::toDto)
                .collect(Collectors.toList());

        log.info("Found {} event(s) for user {}", events.size(), userId);
        return events;
    }

    private void validateUserId(Long userId) {
        log.debug("Validating user ID: {}", userId);
        if (userId == null || userId <= 0) {
            log.warn("Invalid user ID: value={}", userId);
            throw new IllegalArgumentException("User ID must be positive");
        }
    }

    private void validateEventType(EventType eventType) {
        log.debug("Validating event type: {}", eventType);
        if (eventType == null) {
            log.warn("Event type is null");
            throw new IllegalArgumentException("Event type не может быть null");
        }
    }

    private void validateOperation(Operation operation) {
        log.debug("Validating operation: {}", operation);
        if (operation == null) {
            log.warn("Operation is null");
            throw new IllegalArgumentException("Operation не может быть null");
        }
    }
}