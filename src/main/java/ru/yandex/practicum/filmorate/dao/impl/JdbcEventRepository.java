package ru.yandex.practicum.filmorate.dao.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dao.EventRepository;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JdbcEventRepository implements EventRepository {

    private final JdbcTemplate jdbc;
    private final EventRowMapper eventRowMapper;

    private static final String INSERT_EVENT = """
            INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String SELECT_FEED = """
            SELECT event_id, timestamp, user_id, event_type, operation, entity_id
            FROM events
            WHERE user_id = ?
            ORDER BY timestamp ASC
            """;

    @Override
    public void addEvent(Long userId, EventType eventType, Operation operation, Long entityId) {
        jdbc.update(INSERT_EVENT, System.currentTimeMillis(), userId, eventType.name(), operation.name(), entityId);
    }

    @Override
    public List<Event> getFeed(Long userId) {
        return jdbc.query(SELECT_FEED, eventRowMapper, userId);
    }
}