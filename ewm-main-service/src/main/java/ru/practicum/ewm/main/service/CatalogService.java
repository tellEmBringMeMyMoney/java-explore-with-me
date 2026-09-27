package ru.practicum.ewm.main.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.main.model.CategoryEntity;
import ru.practicum.ewm.main.model.UserEntity;
import ru.practicum.ewm.main.repository.CategoryRepository;
import ru.practicum.ewm.main.repository.EventRepository;
import ru.practicum.ewm.main.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
public class CatalogService {
    private final UserRepository users;
    private final CategoryRepository categories;
    private final EventRepository events;

    public CatalogService(UserRepository u, CategoryRepository c, EventRepository e) {
        users = u;
        categories = c;
        events = e;
    }

    public Map<String, Object> user(UserEntity u) {
        return Map.of("id", u.getId(), "name", u.getName(), "email", u.getEmail());
    }

    public Map<String, Object> category(CategoryEntity c) {
        return Map.of("id", c.getId(), "name", c.getName());
    }

    @Transactional
    public Map<String, Object> addUser(Map<String, Object> b) {
        if (b == null) throw new IllegalArgumentException("Request body is required");
        String name = stringField(b, "name"), email = stringField(b, "email");
        if (name.length() < 2 || name.length() > 250)
            throw new IllegalArgumentException("name must contain 2..250 characters");
        if (email.length() < 6 || email.length() > 254 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("email is invalid");
        return user(users.save(new UserEntity(name, email)));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> users(List<Long> ids, int from, int size) {
        validatePagination(from, size);
        var p = new OffsetPageRequest(from, size, org.springframework.data.domain.Sort.by("id"));
        return (ids == null || ids.isEmpty() ? users.findAll(p).getContent() : users.findByIdIn(ids, p)).stream().map(this::user).toList();
    }

    @Transactional
    public void removeUser(long id) {
        users.delete(users.findById(id).orElseThrow(() -> new NotFoundException("User with id=" + id + " was not found")));
    }

    @Transactional
    public Map<String, Object> addCategory(Map<String, Object> b) {
        if (b == null) throw new IllegalArgumentException("Request body is required");
        String name = validName(stringField(b, "name"));
        return category(categories.save(new CategoryEntity(name)));
    }

    @Transactional
    public Map<String, Object> editCategory(long id, Map<String, Object> b) {
        if (b == null) throw new IllegalArgumentException("Request body is required");
        var c = categories.findById(id).orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found"));
        c.setName(validName(stringField(b, "name")));
        return category(c);
    }

    @Transactional
    public void removeCategory(long id) {
        if (events.countByCategoryId(id) > 0) throw new ConflictException("The category is not empty");
        categories.delete(categories.findById(id).orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found")));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> categories(int from, int size) {
        validatePagination(from, size);
        return categories.findAll(new OffsetPageRequest(from, size, org.springframework.data.domain.Sort.by("id"))).map(this::category).toList();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> categoryById(long id) {
        return category(categories.findById(id).orElseThrow(() -> new NotFoundException("Category with id=" + id + " was not found")));
    }

    private String validName(String name) {
        if (name == null || name.isBlank() || name.length() > 50)
            throw new IllegalArgumentException("name must contain 1..50 characters");
        return name;
    }

    private String stringField(Map<String, Object> body, String name) {
        Object value = body.get(name);
        if (!(value instanceof String text)) throw new IllegalArgumentException(name + " must be a string");
        return text;
    }

    private void validatePagination(int from, int size) {
        if (from < 0 || size <= 0) throw new IllegalArgumentException("Invalid pagination values");
    }
}
