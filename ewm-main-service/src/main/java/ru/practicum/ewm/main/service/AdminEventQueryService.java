package ru.practicum.ewm.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.repository.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class AdminEventQueryService {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository events;
    private final EventDtoMapper mapper;

    public AdminEventQueryService(EventRepository e, EventDtoMapper mapper) {
        events = e;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> search(List<Long> users, List<String> states, List<Long> categories, String start, String end, int from, int size) {
        List<EventState> enums = states == null || states.isEmpty() ? null : states.stream().map(EventState::valueOf).toList();
        LocalDateTime a = start == null ? null : LocalDateTime.parse(start, F), b = end == null ? null : LocalDateTime.parse(end, F);
        org.springframework.data.jpa.domain.Specification<EventEntity> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("category");
                root.fetch("initiator");
            }
            var p = cb.conjunction();
            if (users != null && !users.isEmpty()) p = cb.and(p, root.get("initiator").get("id").in(users));
            if (enums != null) p = cb.and(p, root.get("state").in(enums));
            if (categories != null && !categories.isEmpty())
                p = cb.and(p, root.get("category").get("id").in(categories));
            if (a != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("eventDate"), a));
            if (b != null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("eventDate"), b));
            return p;
        };
        if (from < 0 || size <= 0) throw new IllegalArgumentException("Invalid pagination values");
        var list = events.findAll(spec, new OffsetPageRequest(from, size, org.springframework.data.domain.Sort.by("id"))).getContent();
        var mapped = mapper.mapAll(list, true);
        return list.stream().map(e -> mapped.get(e.getId())).toList();
    }
}
