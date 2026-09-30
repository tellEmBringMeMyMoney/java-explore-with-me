package ru.practicum.ewm.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.service.AdminEventQueryService;
import ru.practicum.ewm.main.service.EventCommandService;

import java.util.List;
import java.util.Map;

@RestController
public class EventCommandController {
    private final EventCommandService service;
    private final AdminEventQueryService adminQuery;

    public EventCommandController(EventCommandService s, AdminEventQueryService q) {
        service = s;
        adminQuery = q;
    }

    @GetMapping("/admin/events")
    public List<Map<String, Object>> adminSearch(@RequestParam(required = false) List<Long> users, @RequestParam(required = false) List<String> states, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) String rangeStart, @RequestParam(required = false) String rangeEnd, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return adminQuery.search(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/users/{userId}/events")
    public List<Map<String, Object>> mine(@PathVariable long userId, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return service.mine(userId, from, size);
    }

    @PostMapping("/users/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@PathVariable long userId, @RequestBody Map<String, Object> b) {
        return service.create(userId, b);
    }

    @GetMapping("/users/{userId}/events/{eventId}")
    public Map<String, Object> mineOne(@PathVariable long userId, @PathVariable long eventId) {
        return service.mineOne(userId, eventId);
    }

    @PatchMapping("/users/{userId}/events/{eventId}")
    public Map<String, Object> updateMine(@PathVariable long userId, @PathVariable long eventId, @RequestBody Map<String, Object> b) {
        return service.updateMine(userId, eventId, b);
    }

    @PatchMapping("/admin/events/{eventId}")
    public Map<String, Object> updateAdmin(@PathVariable long eventId, @RequestBody Map<String, Object> b) {
        return service.updateAdmin(eventId, b);
    }
}
