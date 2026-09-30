package ru.practicum.ewm.main.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.dto.CommentRequest;
import ru.practicum.ewm.main.dto.ReactionRequest;
import ru.practicum.ewm.main.service.CommentService;

import java.util.List;
import java.util.Map;

@RestController
public class CommentController {
    private final CommentService service;
    public CommentController(CommentService service) { this.service = service; }

    @PostMapping("/users/{userId}/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@PathVariable long userId, @PathVariable long eventId, @Valid @RequestBody CommentRequest request) {
        return service.create(userId, eventId, request);
    }
    @GetMapping("/events/{eventId}/comments")
    public List<Map<String, Object>> list(@PathVariable long eventId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return service.byEvent(eventId, from, size);
    }
    @PatchMapping("/users/{userId}/comments/{commentId}")
    public Map<String, Object> update(@PathVariable long userId, @PathVariable long commentId, @Valid @RequestBody CommentRequest request) {
        return service.update(userId, commentId, request);
    }
    @PutMapping("/users/{userId}/comments/{commentId}/reaction")
    public Map<String, Object> react(@PathVariable long userId, @PathVariable long commentId, @Valid @RequestBody ReactionRequest request) {
        return service.react(userId, commentId, request);
    }
}
