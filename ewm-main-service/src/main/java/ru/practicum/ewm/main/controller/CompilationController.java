package ru.practicum.ewm.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.service.CompilationService;

import java.util.List;
import java.util.Map;

@RestController
public class CompilationController {
    private final CompilationService service;

    public CompilationController(CompilationService s) {
        service = s;
    }

    @PostMapping("/admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Map<String, Object> b) {
        return service.create(b);
    }

    @PatchMapping("/admin/compilations/{compId}")
    public Map<String, Object> update(@PathVariable long compId, @RequestBody(required = false) Map<String, Object> b) {
        return service.update(compId, b);
    }

    @DeleteMapping("/admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long compId) {
        service.delete(compId);
    }

    @GetMapping("/compilations")
    public List<Map<String, Object>> list(@RequestParam(required = false) Boolean pinned, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return service.list(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public Map<String, Object> get(@PathVariable long compId) {
        return service.get(compId);
    }
}
