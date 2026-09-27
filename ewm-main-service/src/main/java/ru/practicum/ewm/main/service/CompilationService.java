package ru.practicum.ewm.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.model.CompilationEntity;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.repository.CompilationRepository;
import ru.practicum.ewm.main.repository.EventRepository;

import java.util.*;

@Service
public class CompilationService {
    private final CompilationRepository compilations;
    private final EventRepository events;
    private final EventDtoMapper eventMapper;

    public CompilationService(CompilationRepository c, EventRepository e, EventDtoMapper mapper) {
        compilations = c;
        events = e;
        eventMapper = mapper;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> b) {
        validate(b, true);
        CompilationEntity c = new CompilationEntity((String) b.get("title"), booleanValue(b.get("pinned"), false));
        if (b.get("events") instanceof List<?> ids) c.setEvents(resolve(ids));
        return dtos(List.of(compilations.save(c)), true).get(0);
    }

    @Transactional
    public Map<String, Object> update(long id, Map<String, Object> b) {
        if (b == null) b = Map.of();
        validate(b, false);
        CompilationEntity c = getEntity(id);
        if (b.get("title") != null) c.setTitle((String) b.get("title"));
        if (b.get("pinned") != null) c.setPinned(booleanValue(b.get("pinned"), c.isPinned()));
        if (b.get("events") instanceof List<?> ids) c.setEvents(resolve(ids));
        return dtos(List.of(c), true).get(0);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> list(Boolean pinned, int from, int size) {
        if (from < 0 || size <= 0) throw new IllegalArgumentException("Invalid pagination values");
        org.springframework.data.domain.Pageable p = new OffsetPageRequest(from, size, org.springframework.data.domain.Sort.by("id"));
        return dtos((pinned == null ? compilations.findAll(p) : compilations.findByPinned(pinned, p)).getContent(), false);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(long id) {
        return dtos(List.of(getEntity(id)), false).get(0);
    }

    @Transactional
    public void delete(long id) {
        compilations.delete(getEntity(id));
    }

    private CompilationEntity getEntity(long id) {
        return compilations.findById(id).orElseThrow(() -> new NotFoundException("Compilation with id=" + id + " was not found"));
    }

    private Set<EventEntity> resolve(List<?> ids) {
        Set<EventEntity> set = new LinkedHashSet<>();
        for (Object value : ids) {
            long id;
            if (value instanceof Number number) {
                id = number.longValue();
            } else if (value instanceof String text) {
                try {
                    id = Long.parseLong(text);
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("events must contain event IDs");
                }
            } else {
                throw new IllegalArgumentException("events must contain event IDs");
            }
            if (!set.add(events.findById(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"))))
                throw new IllegalArgumentException("events must not contain duplicates");
        }
        return set;
    }

    private void validate(Map<String, Object> b, boolean create) {
        if (b == null) throw new IllegalArgumentException("Request body is required");
        Object title = b.get("title");
        if (create && !(title instanceof String)) throw new IllegalArgumentException("title is required");
        if (title != null && (!(title instanceof String s) || s.isBlank() || s.length() > 50))
            throw new IllegalArgumentException("title must contain 1..50 characters");
        if (b.get("pinned") != null && !isBooleanValue(b.get("pinned")))
            throw new IllegalArgumentException("pinned must be boolean");
        if (b.get("events") != null && !(b.get("events") instanceof List<?>))
            throw new IllegalArgumentException("events must be an array");
        if (b.get("events") instanceof List<?> ids && new HashSet<>(ids).size() != ids.size())
            throw new IllegalArgumentException("events must not contain duplicates");
    }

    private boolean isBooleanValue(Object value) {
        return value instanceof Boolean || value instanceof String s && ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s));
    }

    private boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) return defaultValue;
        return value instanceof Boolean b ? b : Boolean.parseBoolean((String) value);
    }

    private List<Map<String, Object>> dtos(List<CompilationEntity> compilations, boolean includeUnpublished) {
        List<EventEntity> all = compilations.stream().flatMap(c -> c.getEvents().stream())
                .filter(e -> includeUnpublished || e.getState() == EventState.PUBLISHED).distinct().toList();
        Map<Long, Map<String, Object>> mapped = eventMapper.mapAll(all, false);
        return compilations.stream().map(c -> Map.<String, Object>of("id", c.getId(), "title", c.getTitle(), "pinned", c.isPinned(), "events", c.getEvents().stream().filter(e -> includeUnpublished || e.getState() == EventState.PUBLISHED).map(e -> mapped.get(e.getId())).toList())).toList();
    }
}
