package ru.practicum.ewm.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class EventDtoMapper {
    private static final Logger log = LoggerFactory.getLogger(EventDtoMapper.class);
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final RequestRepository requests;
    private final StatsClient stats;

    public EventDtoMapper(RequestRepository requests, StatsClient stats) {
        this.requests = requests;
        this.stats = stats;
    }

    public Map<Long, Map<String, Object>> mapAll(Collection<EventEntity> source, boolean full) {
        List<EventEntity> events = List.copyOf(source);
        return mapAll(events, full, viewsByEventIds(events.stream().map(EventEntity::getId).toList()));
    }

    public Map<Long, Map<String, Object>> mapAll(Collection<EventEntity> source, boolean full, Map<Long, Long> views) {
        List<EventEntity> events = List.copyOf(source);
        if (events.isEmpty()) return Map.of();
        List<Long> ids = events.stream().map(EventEntity::getId).toList();
        Map<Long, Long> confirmed = requests.countConfirmedForEvents(ids).stream().collect(Collectors.toMap(RequestRepository.EventRequestCount::getEventId, RequestRepository.EventRequestCount::getCount));
        Map<Long, Map<String, Object>> mapped = new LinkedHashMap<>();
        for (EventEntity e : events)
            mapped.put(e.getId(), dto(e, full, confirmed.getOrDefault(e.getId(), 0L), views.getOrDefault(e.getId(), 0L)));
        return mapped;
    }

    public Map<Long, Long> viewsByEventIds(List<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        Map<Long, Long> result = new HashMap<>();
        for (int i = 0; i < ids.size(); i += 200) {
            List<String> uris = ids.subList(i, Math.min(i + 200, ids.size())).stream().map(id -> "/events/" + id).toList();
            try {
                for (ViewStats row : stats.getStats(LocalDateTime.of(2000, 1, 1, 0, 0), LocalDateTime.now().plusSeconds(1), uris, true)) {
                    try {
                        result.put(Long.parseLong(row.uri().substring("/events/".length())), row.hits());
                    } catch (NumberFormatException ignored) {
                    }
                }
            } catch (RestClientException ex) {
                log.warn("Could not load event views from statistics service", ex);
                return Map.of();
            }
        }
        return result;
    }

    public Map<String, Object> map(EventEntity event, boolean full) {
        return mapAll(List.of(event), full).get(event.getId());
    }

    private Map<String, Object> dto(EventEntity e, boolean full, long confirmed, long views) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", e.getId());
        d.put("annotation", e.getAnnotation());
        d.put("category", Map.of("id", e.getCategory().getId(), "name", e.getCategory().getName()));
        d.put("confirmedRequests", confirmed);
        d.put("eventDate", FORMAT.format(e.getEventDate()));
        d.put("initiator", Map.of("id", e.getInitiator().getId(), "name", e.getInitiator().getName()));
        d.put("paid", e.isPaid());
        d.put("title", e.getTitle());
        d.put("views", views);
        if (full) {
            d.put("createdOn", FORMAT.format(e.getCreatedOn()));
            d.put("description", e.getDescription());
            d.put("location", Map.of("lat", e.getLat(), "lon", e.getLon()));
            d.put("participantLimit", e.getParticipantLimit());
            d.put("publishedOn", e.getPublishedOn() == null ? null : FORMAT.format(e.getPublishedOn()));
            d.put("requestModeration", e.isRequestModeration());
            d.put("state", e.getState().name());
        }
        return d;
    }
}
