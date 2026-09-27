package ru.practicum.ewm.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.RequestEntity;
import ru.practicum.ewm.main.model.UserEntity;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class RequestService {
    private final UserRepository users;
    private final EventRepository events;
    private final RequestRepository requests;

    public RequestService(UserRepository u, EventRepository e, RequestRepository r) {
        users = u;
        events = e;
        requests = r;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> byUser(long userId) {
        user(userId);
        return requests.findByRequesterIdOrderByCreated(userId).stream().map(this::dto).toList();
    }

    @Transactional
    public Map<String, Object> add(long uid, long eid) {
        UserEntity u = user(uid);
        EventEntity e = events.findLockedDetailed(eid).orElseThrow(() -> new NotFoundException("Event with id=" + eid + " was not found"));
        if (e.getInitiator().getId().equals(uid)) throw new ConflictException("Initiator cannot request own event");
        if (e.getState() != EventState.PUBLISHED || e.getEventDate().isBefore(LocalDateTime.now()))
            throw new ConflictException("Event is not available");
        if (requests.existsByEventIdAndRequesterId(eid, uid)) throw new ConflictException("Request already exists");
        long count = requests.countByEventIdAndStatus(eid, "CONFIRMED");
        if (e.getParticipantLimit() > 0 && count >= e.getParticipantLimit())
            throw new ConflictException("Participant limit reached");
        String status = !e.isRequestModeration() || e.getParticipantLimit() == 0 ? "CONFIRMED" : "PENDING";
        return dto(requests.save(new RequestEntity(LocalDateTime.now(), e, u, status)));
    }

    @Transactional
    public Map<String, Object> cancel(long uid, long rid) {
        RequestEntity r = requests.findById(rid).orElseThrow(() -> new NotFoundException("Request with id=" + rid + " was not found"));
        if (!r.getRequester().getId().equals(uid))
            throw new NotFoundException("Request with id=" + rid + " was not found");
        if (!"PENDING".equals(r.getStatus()))
            throw new ConflictException("Only pending requests can be canceled");
        r.setStatus("CANCELED");
        return dto(r);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> forEvent(long uid, long eid) {
        EventEntity e = event(eid);
        if (!e.getInitiator().getId().equals(uid))
            throw new ForbiddenException("Only event initiator can view requests");
        return requests.findByEventIdOrderByCreated(eid).stream().map(this::dto).toList();
    }

    @Transactional
    public Map<String, Object> moderate(long uid, long eid, Map<String, Object> b) {
        EventEntity e = events.findLockedDetailed(eid).orElseThrow(() -> new NotFoundException("Event with id=" + eid + " was not found"));
        if (!e.getInitiator().getId().equals(uid))
            throw new ForbiddenException("Only event initiator can manage requests");
        String target = String.valueOf(b.get("status"));
        if (!Set.of("CONFIRMED", "REJECTED").contains(target))
            throw new IllegalArgumentException("Invalid request status");
        List<?> ids = (List<?>) b.getOrDefault("requestIds", List.of());
        List<Map<String, Object>> confirmed = new ArrayList<>(), rejected = new ArrayList<>();
        long count = requests.countByEventIdAndStatus(eid, "CONFIRMED");
        for (Object raw : ids) {
            long id = ((Number) raw).longValue();
            RequestEntity r = requests.findById(id).orElseThrow(() -> new NotFoundException("Request with id=" + id + " was not found"));
            if (!r.getEvent().getId().equals(eid) || !"PENDING".equals(r.getStatus()))
                throw new ConflictException("Request is not pending for this event");
            if ("CONFIRMED".equals(target) && e.getParticipantLimit() > 0 && count >= e.getParticipantLimit()) {
                r.setStatus("REJECTED");
                rejected.add(dto(r));
            } else {
                r.setStatus(target);
                if ("CONFIRMED".equals(target)) {
                    count++;
                    confirmed.add(dto(r));
                } else rejected.add(dto(r));
            }
        }
        if (e.getParticipantLimit() > 0 && count >= e.getParticipantLimit())
            for (RequestEntity r : requests.findByEventIdOrderByCreated(eid))
                if ("PENDING".equals(r.getStatus())) {
                    r.setStatus("REJECTED");
                    rejected.add(dto(r));
                }
        return Map.of("confirmedRequests", confirmed, "rejectedRequests", rejected);
    }

    private UserEntity user(long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User with id=" + id + " was not found"));
    }

    private EventEntity event(long id) {
        return events.findDetailed(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
    }

    private Map<String, Object> dto(RequestEntity r) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("id", r.getId());
        d.put("created", r.getCreated().toString());
        d.put("event", r.getEvent().getId());
        d.put("requester", r.getRequester().getId());
        d.put("status", r.getStatus());
        return d;
    }
}
