package ru.practicum.ewm.stats.controller;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class StatsController {
    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void hit(@Valid @RequestBody EndpointHit hit) {
        service.save(hit);
    }

    @GetMapping("/stats")
    public List<ViewStats> getStats(
            @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(value = "uris", required = false) List<String> uris,
            @RequestParam(value = "unique", defaultValue = "false") boolean unique) {
        return service.getStats(start, end, uris, unique);
    }
}
