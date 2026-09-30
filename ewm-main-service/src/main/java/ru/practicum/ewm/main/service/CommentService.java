package ru.practicum.ewm.main.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.dto.CommentRequest;
import ru.practicum.ewm.main.dto.ReactionRequest;
import ru.practicum.ewm.main.model.*;
import ru.practicum.ewm.main.repository.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class CommentService {
    private final CommentRepository comments;
    private final CommentReactionRepository reactions;
    private final UserRepository users;
    private final EventRepository events;

    public CommentService(CommentRepository comments, CommentReactionRepository reactions, UserRepository users, EventRepository events) {
        this.comments = comments; this.reactions = reactions; this.users = users; this.events = events;
    }

    @Transactional
    public Map<String, Object> create(long userId, long eventId, CommentRequest request) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        EventEntity event = events.findById(eventId).orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
        return dto(comments.save(new CommentEntity(request.text().trim(), now(), user, event)), 0, 0);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> byEvent(long eventId, int from, int size) {
        if (from < 0 || size < 1 || size > 100) throw new IllegalArgumentException("from must be non-negative and size must be between 1 and 100");
        if (!events.existsById(eventId)) throw new NotFoundException("Event with id=" + eventId + " was not found");
        Page<CommentEntity> page = comments.findByEventIdOrderByCreatedDescIdDesc(eventId,
                new OffsetPageRequest(from, size, Sort.by(Sort.Direction.DESC, "created", "id")));
        List<Long> ids = page.getContent().stream().map(CommentEntity::getId).toList();
        Map<Long, long[]> counts = new HashMap<>();
        if (!ids.isEmpty()) for (CommentReactionRepository.Counts row : reactions.countByComments(ids)) {
            long[] pair = counts.computeIfAbsent(row.getCommentId(), ignored -> new long[2]);
            if (ReactionType.FIRE == row.getType()) pair[0] = row.getCount(); else pair[1] = row.getCount();
        }
        return page.getContent().stream().map(c -> {
            long[] pair = counts.getOrDefault(c.getId(), new long[2]);
            return dto(c, pair[0], pair[1]);
        }).toList();
    }

    @Transactional
    public Map<String, Object> update(long userId, long commentId, CommentRequest request) {
        CommentEntity comment = comment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) throw new ForbiddenException("Only the comment author can edit it");
        comment.updateText(request.text().trim(), now());
        return dto(comment, counts(commentId, ReactionType.FIRE), counts(commentId, ReactionType.AHH));
    }

    @Transactional
    public Map<String, Object> react(long userId, long commentId, ReactionRequest request) {
        UserEntity user = users.findById(userId).orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));
        CommentEntity comment = comment(commentId);
        CommentReactionEntity reaction = reactions.findByCommentIdAndUserId(commentId, userId)
                .orElseGet(() -> new CommentReactionEntity(comment, user, request.reaction()));
        reaction.setType(request.reaction());
        reactions.save(reaction);
        return dto(comment, counts(commentId, ReactionType.FIRE), counts(commentId, ReactionType.AHH));
    }

    private long counts(long commentId, ReactionType type) {
        return reactions.countByComments(List.of(commentId)).stream().filter(r -> type == r.getType()).mapToLong(CommentReactionRepository.Counts::getCount).findFirst().orElse(0);
    }
    private CommentEntity comment(long id) { return comments.findById(id).orElseThrow(() -> new NotFoundException("Comment with id=" + id + " was not found")); }
    private LocalDateTime now() { return LocalDateTime.now().truncatedTo(ChronoUnit.MICROS); }
    private Map<String, Object> dto(CommentEntity c, long fire, long ahh) {
        Map<String, Object> author = Map.of("id", c.getAuthor().getId(), "name", c.getAuthor().getName());
        return Map.of("id", c.getId(), "text", c.getText(), "author", author, "event", c.getEvent().getId(),
                "created", c.getCreated().toString(), "updated", c.getUpdated().toString(), "reactions", Map.of("fire", fire, "ahh", ahh));
    }
}
