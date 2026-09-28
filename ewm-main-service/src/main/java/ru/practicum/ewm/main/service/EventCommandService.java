package ru.practicum.ewm.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.model.CategoryEntity;
import ru.practicum.ewm.main.model.EventEntity;
import ru.practicum.ewm.main.model.EventState;
import ru.practicum.ewm.main.model.UserEntity;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.RequestRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class EventCommandService {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final UserRepository users;
    private final CategoryRepository categories;
    private final EventRepository events;
    private final RequestRepository requests;
    private final EventDtoMapper mapper;

    public EventCommandService(UserRepository u, CategoryRepository c, EventRepository e, RequestRepository r, EventDtoMapper mapper) {
        users = u;
        categories = c;
        events = e;
        requests = r;
        this.mapper = mapper;
    }

    @Transactional
    public Map<String, Object> create(long uid, Map<String, Object> b) {
        validateNewEvent(b);
        UserEntity u = users.findById(uid).orElseThrow(() -> new NotFoundException("User with id=" + uid + " was not found"));
        CategoryEntity c = category(((Number) b.get("category")).longValue());
        LocalDateTime date = parseDate((String) b.get("eventDate"));
        if (date.isBefore(LocalDateTime.now().plusHours(2)))
            throw new IllegalArgumentException("Event date must be at least two hours in the future");
        Map<String, Object> l = (Map<String, Object>) b.get("location");
        EventEntity e = new EventEntity((String) b.get("annotation"), (String) b.get("description"), date, LocalDateTime.now(), ((Number) l.get("lat")).doubleValue(), ((Number) l.get("lon")).doubleValue(), booleanValue(b.get("paid"), false), integerValue(b.get("participantLimit"), 0), booleanValue(b.get("requestModeration"), true), (String) b.get("title"), c, u);
        return dto(events.save(e));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> mine(long uid, int from, int size) {
        validatePagination(from, size);
        if (!users.existsById(uid)) throw new NotFoundException("User with id=" + uid + " was not found");
        var list = events.findByInitiator(uid, new OffsetPageRequest(from, size, org.springframework.data.domain.Sort.by("id")));
        var mapped = mapper.mapAll(list, true);
        return list.stream().map(e -> mapped.get(e.getId())).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> mineOne(long uid, long id) {
        EventEntity e = event(id);
        if (!e.getInitiator().getId().equals(uid))
            throw new NotFoundException("Event with id=" + id + " was not found");
        return dto(e);
    }

    @Transactional
    public Map<String, Object> updateMine(long uid, long id, Map<String, Object> b) {
        EventEntity e = mineEntity(uid, id);
        if (e.getState() == EventState.PUBLISHED)
            throw new ConflictException("Only pending or canceled events can be changed");
        StateAction action = validateUpdate(b, false);
        apply(e, b);
        if (b.get("eventDate") != null && e.getEventDate().isBefore(LocalDateTime.now().plusHours(2)))
            throw new IllegalArgumentException("Event date must be at least two hours in the future");
        if (e.getParticipantLimit() > 0 && requests.countByEventIdAndStatus(id, "CONFIRMED") > e.getParticipantLimit())
            throw new ConflictException("Participant limit cannot be lower than confirmed requests");
        if (action == StateAction.CANCEL_REVIEW) e.setState(EventState.CANCELED);
        if (action == StateAction.SEND_TO_REVIEW) e.setState(EventState.PENDING);
        return dto(e);
    }

    @Transactional
    public Map<String, Object> updateAdmin(long id, Map<String, Object> b) {
        EventEntity e = event(id);
        StateAction action = validateUpdate(b, true);
        apply(e, b);
        if (b.get("eventDate") != null && e.getEventDate().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Event date must be in the future");
        if (action == StateAction.PUBLISH_EVENT) {
            if (e.getState() != EventState.PENDING) throw new ConflictException("Event is not pending");
            if (e.getEventDate().isBefore(LocalDateTime.now().plusHours(1)))
                throw new ConflictException("Event date is too soon to publish");
            e.setState(EventState.PUBLISHED);
            e.setPublishedOn(LocalDateTime.now());
        } else if (action == StateAction.REJECT_EVENT) {
            if (e.getState() == EventState.PUBLISHED) throw new ConflictException("Published event cannot be rejected");
            e.setState(EventState.CANCELED);
        }
        return dto(e);
    }

    private void apply(EventEntity e, Map<String, Object> b) {
        if (b.get("annotation") != null) e.setAnnotation((String) b.get("annotation"));
        if (b.get("description") != null) e.setDescription((String) b.get("description"));
        if (b.get("title") != null) e.setTitle((String) b.get("title"));
        if (b.get("eventDate") != null) e.setEventDate(parseDate((String) b.get("eventDate")));
        if (b.get("category") != null) e.setCategory(category(((Number) b.get("category")).longValue()));
        if (b.get("paid") != null) e.setPaid(booleanValue(b.get("paid"), e.isPaid()));
        if (b.get("participantLimit") != null) e.setParticipantLimit(integerValue(b.get("participantLimit"), e.getParticipantLimit()));
        if (b.get("requestModeration") != null) e.setRequestModeration(booleanValue(b.get("requestModeration"), e.isRequestModeration()));
        if (b.get("location") instanceof Map<?, ?> l) {
            e.setLat(((Number) l.get("lat")).doubleValue());
            e.setLon(((Number) l.get("lon")).doubleValue());
        }
    }

    private EventEntity mineEntity(long uid, long id) {
        EventEntity e = event(id);
        if (!e.getInitiator().getId().equals(uid))
            throw new NotFoundException("Event with id=" + id + " was not found");
        return e;
    }

    private EventEntity event(long id) {
        return events.findDetailed(id).orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));
    }

    private CategoryEntity category(long id) {
        return categories.findById(id).orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
    }

    private void validatePagination(int from, int size) {
        if (from < 0) throw new IllegalArgumentException("from must be non-negative");
        if (size <= 0) throw new IllegalArgumentException("size must be positive");
    }

    private LocalDateTime parseDate(String value) {
        try {
            return LocalDateTime.parse(value, F);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("eventDate must use yyyy-MM-dd HH:mm:ss");
        }
    }

    private void validateNewEvent(Map<String, Object> b) {
        if (b == null) throw new IllegalArgumentException("Request body is required");
        validateText(b, "annotation", 20, 2000, true);
        validateText(b, "description", 20, 7000, true);
        validateText(b, "title", 3, 120, true);
        if (!(b.get("category") instanceof Number)) throw new IllegalArgumentException("category is required");
        if (!(b.get("location") instanceof Map<?, ?> location) || !(location.get("lat") instanceof Number) || !(location.get("lon") instanceof Number))
            throw new IllegalArgumentException("location requires numeric lat and lon");
        if (!(b.get("eventDate") instanceof String s)) throw new IllegalArgumentException("eventDate is required");
        parseDate(s);
        int limit = integerValue(b.get("participantLimit"), 0);
        if (limit < 0) throw new IllegalArgumentException("participantLimit must be non-negative");
        if (b.get("paid") != null && !isBooleanValue(b.get("paid")))
            throw new IllegalArgumentException("paid must be boolean");
        if (b.get("requestModeration") != null && !isBooleanValue(b.get("requestModeration")))
            throw new IllegalArgumentException("requestModeration must be boolean");
    }

    private StateAction validateUpdate(Map<String, Object> b, boolean admin) {
        for (String key : List.of("annotation", "description", "title"))
            if (b.get(key) != null) {
                if (!(b.get(key) instanceof String)) throw new IllegalArgumentException(key + " must be a string");
                validateText(b, key, "annotation".equals(key) ? 20 : "title".equals(key) ? 3 : 20, "annotation".equals(key) ? 2000 : "description".equals(key) ? 7000 : 120, false);
            }
        if (b.get("eventDate") instanceof String s) parseDate(s);
        if (b.get("eventDate") != null && !(b.get("eventDate") instanceof String))
            throw new IllegalArgumentException("eventDate must be a string");
        if (b.get("participantLimit") != null && integerValue(b.get("participantLimit"), 0) < 0)
            throw new IllegalArgumentException("participantLimit must be non-negative");
        if (b.get("category") != null && !(b.get("category") instanceof Number))
            throw new IllegalArgumentException("category must be an integer");
        if (b.get("paid") != null && !isBooleanValue(b.get("paid")))
            throw new IllegalArgumentException("paid must be boolean");
        if (b.get("requestModeration") != null && !isBooleanValue(b.get("requestModeration")))
            throw new IllegalArgumentException("requestModeration must be boolean");
        if (b.get("location") != null && (!(b.get("location") instanceof Map<?, ?> l) || !(l.get("lat") instanceof Number) || !(l.get("lon") instanceof Number)))
            throw new IllegalArgumentException("location requires numeric lat and lon");
        return parseStateAction(b.get("stateAction"), admin);
    }

    private StateAction parseStateAction(Object raw, boolean admin) {
        if (raw == null) return null;
        if (!(raw instanceof String value)) throw new IllegalArgumentException("Invalid stateAction");
        StateAction action;
        try {
            action = StateAction.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid stateAction");
        }
        if (action.isAdminOnly() != admin) throw new IllegalArgumentException("Invalid stateAction");
        return action;
    }

    private void validateText(Map<String, Object> b, String key, int min, int max, boolean required) {
        Object raw = b.get(key);
        if (raw == null) {
            if (required) throw new IllegalArgumentException(key + " is required");
            return;
        }
        if (!(raw instanceof String s) || s.isBlank() || s.length() < min || s.length() > max)
            throw new IllegalArgumentException(key + " must contain " + min + ".." + max + " characters");
    }

    private boolean isBooleanValue(Object value) {
        return value instanceof Boolean || value instanceof String s && ("true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s));
    }

    private int integerValue(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        if (value instanceof Number number) {
            double numericValue = number.doubleValue();
            if (!Double.isFinite(numericValue) || numericValue != Math.rint(numericValue)
                    || numericValue < Integer.MIN_VALUE || numericValue > Integer.MAX_VALUE)
                throw new IllegalArgumentException("participantLimit must be an integer");
            return (int) numericValue;
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("participantLimit must be an integer");
            }
        }
        throw new IllegalArgumentException("participantLimit must be an integer");
    }

    private boolean booleanValue(Object value, boolean defaultValue) {
        if (value == null) return defaultValue;
        return value instanceof Boolean b ? b : Boolean.parseBoolean((String) value);
    }

    private Map<String, Object> dto(EventEntity e) {
        return mapper.map(e, true);
    }
}
