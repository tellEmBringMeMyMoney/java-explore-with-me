package ru.practicum.ewm.stats.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class, DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest() {
    }
}
