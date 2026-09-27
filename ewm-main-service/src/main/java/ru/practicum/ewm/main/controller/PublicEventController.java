package ru.practicum.ewm.main.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.ewm.main.service.PublicEventService;

import java.util.List;
import java.util.Map;

@RestController
public class PublicEventController {
    private final PublicEventService service;

    public PublicEventController(PublicEventService service) {
        this.service = service;
    }

    @GetMapping("/events")
    public List<Map<String, Object>> search(@RequestParam(required = false) String text, @RequestParam(required = false) List<Long> categories, @RequestParam(required = false) Boolean paid, @RequestParam(required = false) String rangeStart, @RequestParam(required = false) String rangeEnd, @RequestParam(defaultValue = "false") boolean onlyAvailable, @RequestParam(required = false) String sort, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        return service.search(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/events/{id}")
    public Map<String, Object> get(@PathVariable long id, HttpServletRequest request) {
        return service.get(id, request);
    }
}
