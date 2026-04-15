package ru.yandex.practicum.filmorate.dao;

import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

public interface EventRepository {
    void addEvent(Long userId, EventType eventType, Operation operation, Long entityId);
    List<Event> getFeed(Long userId);
}