package ru.practicum.ewm.main.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.main.service.CatalogService;

import java.util.List;
import java.util.Map;

@RestController
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories(@RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return service.categories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public Map<String, Object> category(@PathVariable long catId) {
        return service.categoryById(catId);
    }

    @PostMapping("/admin/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addCategory(@RequestBody Map<String, Object> body) {
        return service.addCategory(body);
    }

    @PatchMapping("/admin/categories/{catId}")
    public Map<String, Object> updateCategory(@PathVariable long catId, @RequestBody Map<String, Object> body) {
        return service.editCategory(catId, body);
    }

    @DeleteMapping("/admin/categories/{catId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable long catId) {
        service.removeCategory(catId);
    }

    @GetMapping("/admin/users")
    public List<Map<String, Object>> users(@RequestParam(required = false) List<Long> ids, @RequestParam(defaultValue = "0") int from, @RequestParam(defaultValue = "10") int size) {
        return service.users(ids, from, size);
    }

    @PostMapping("/admin/users")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addUser(@RequestBody Map<String, Object> body) {
        return service.addUser(body);
    }

    @DeleteMapping("/admin/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable long userId) {
        service.removeUser(userId);
    }
}
