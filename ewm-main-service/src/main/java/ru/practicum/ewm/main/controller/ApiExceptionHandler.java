package ru.practicum.ewm.main.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import ru.practicum.ewm.main.service.ConflictException;
import ru.practicum.ewm.main.service.ForbiddenException;
import ru.practicum.ewm.main.service.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> notFound(RuntimeException e) {
        return error("NOT_FOUND", "The required object was not found.", e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> conflict(RuntimeException e) {
        return error("CONFLICT", "For the requested operation the conditions are not met.", e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> forbidden(RuntimeException e) {
        return error("FORBIDDEN", "For the requested operation the conditions are not met.", e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class, HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class, ConstraintViolationException.class, MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> bad(Exception e) {
        return error("BAD_REQUEST", "Incorrectly made request.", e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> integrity(DataIntegrityViolationException e) {
        return error("CONFLICT", "Integrity constraint has been violated.", e.getMostSpecificCause().getMessage());
    }

    private Map<String, Object> error(String status, String reason, String message) {
        return Map.of("status", status, "reason", reason, "message", message == null ? "Bad request" : message, "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), "errors", List.of());
    }
}
