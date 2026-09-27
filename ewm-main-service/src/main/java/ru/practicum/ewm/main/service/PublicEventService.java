package ru.practicum.ewm.main.service;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PublicEventService {
    private static final Logger log = LoggerFactory.getLogger(PublicEventService.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EventRepository events;
    private final StatsClient stats;
    private final EventDtoMapper mapper;

    public PublicEventService(EventRepository e, StatsClient s, EventDtoMapper mapper) {
        events = e;
        stats = s;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> search(String text, List<Long> cats, Boolean paid, String start, String end, boolean onlyAvailable, String sort, int from, int size, HttpServletRequest request) {
        validatePagination(from, size);
        if (sort != null && !Set.of("EVENT_DATE", "VIEWS").contains(sort))
            throw new IllegalArgumentException("Unsupported sort: " + sort);
        if (text != null && (text.isBlank() || text.length() > 7000))
            throw new IllegalArgumentException("text must contain 1..7000 characters");
        recordHit("/events", request);
        LocalDateTime a = start == null ? LocalDateTime.now() : LocalDateTime.parse(start, FORMAT), b = end == null ? null : LocalDateTime.parse(end, FORMAT);
        if (a != null && b != null && a.isAfter(b))
            throw new IllegalArgumentException("rangeStart must be before rangeEnd");
        Pageable pageable = new OffsetPageRequest(from, size, Sort.by("eventDate"));
        org.springframework.data.jpa.domain.Specification<EventEntity> spec = (root, query, cb) -> {
            if (query.getResultType() != Long.class) {
                root.fetch("category");
                root.fetch("initiator");
            }
            var p = cb.equal(root.get("state"), EventState.PUBLISHED);
            if (a != null) p = cb.and(p, cb.greaterThanOrEqualTo(root.get("eventDate"), a));
            if (b != null) p = cb.and(p, cb.lessThanOrEqualTo(root.get("eventDate"), b));
            if (cats != null && !cats.isEmpty()) p = cb.and(p, root.get("category").get("id").in(cats));
            if (paid != null) p = cb.and(p, cb.equal(root.get("paid"), paid));
            if (text != null && !text.isBlank()) {
                String term = "%" + text.toLowerCase() + "%";
                p = cb.and(p, cb.or(cb.like(cb.lower(root.get("annotation")), term), cb.like(cb.lower(root.get("description")), term)));
            }
            if (onlyAvailable) {
                var sq = query.subquery(Long.class);
                var req = sq.from(ru.practicum.ewm.main.model.RequestEntity.class);
                sq.select(cb.count(req.get("id"))).where(cb.equal(req.get("event").get("id"), root.get("id")), cb.equal(req.get("status"), "CONFIRMED"));
                p = cb.and(p, cb.or(cb.equal(root.get("participantLimit"), 0), cb.lessThan(sq, root.get("participantLimit"))));
            }
            return p;
        };
        List<EventEntity> result;
        Map<Long, Long> viewCounts = Map.of();
        if ("VIEWS".equals(sort)) {
            List<Long> ids = new ArrayList<>(events.findIds(spec));
            viewCounts = mapper.viewsByEventIds(ids);
            Map<Long, Long> counts = viewCounts;
            ids.sort(Comparator.comparingLong((Long id) -> counts.getOrDefault(id, 0L)).reversed().thenComparingLong(Long::longValue));
            List<Long> pageIds = ids.stream().skip(from).limit(size).toList();
            Map<Long, EventEntity> byId = new HashMap<>();
            events.findAllById(pageIds).forEach(e -> byId.put(e.getId(), e));
            result = pageIds.stream().map(byId::get).filter(Objects::nonNull).toList();
        } else result = events.findAll(spec, pageable).getContent();
        Map<Long, Map<String, Object>> mapped = "VIEWS".equals(sort) ? mapper.mapAll(result, false, viewCounts) : mapper.mapAll(result, false);
        return result.stream().map(e -> mapped.get(e.getId())).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> get(long id, HttpServletRequest request) {
        recordHit("/events/" + id, request);
        EventEntity e = events.findDetailed(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
        if (e.getState() != EventState.PUBLISHED) throw new NotFoundException("Event with id=" + id + " was not found");
        return mapper.map(e, true);
    }

    private void recordHit(String uri, HttpServletRequest request) {
        try {
            stats.saveHit(new EndpointHit(null, "ewm-main-service", uri, request.getRemoteAddr(), FORMAT.format(LocalDateTime.now())));
        } catch (RestClientException e) {
            log.warn("Statistics service is unavailable while recording {}", uri, e);
        }
    }

    private void validatePagination(int from, int size) {
        if (from < 0) throw new IllegalArgumentException("from must be non-negative");
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
    }
}
