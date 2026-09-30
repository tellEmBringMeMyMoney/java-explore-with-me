package ru.practicum.ewm.main.service;

public class ConflictException extends RuntimeException {
    public ConflictException(String m) {
        super(m);
    }
}
