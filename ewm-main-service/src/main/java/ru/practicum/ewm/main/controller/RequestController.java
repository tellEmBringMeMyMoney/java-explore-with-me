package ru.practicum.ewm.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
public class RequestController {
    private final RequestService service;

    public RequestController(RequestService s) {
        service = s;
    }

    @GetMapping("/users/{userId}/requests")
    public List<Map<String, Object>> requests(@PathVariable long userId) {
        return service.byUser(userId);
    }

    @PostMapping("/users/{userId}/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> add(@PathVariable long userId, @RequestParam long eventId) {
        return service.add(userId, eventId);
    }

    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public Map<String, Object> cancel(@PathVariable long userId, @PathVariable long requestId) {
        return service.cancel(userId, requestId);
    }

    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public List<Map<String, Object>> forEvent(@PathVariable long userId, @PathVariable long eventId) {
        return service.forEvent(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public Map<String, Object> moderate(@PathVariable long userId, @PathVariable long eventId, @RequestBody Map<String, Object> b) {
        return service.moderate(userId, eventId, b);
    }
}
