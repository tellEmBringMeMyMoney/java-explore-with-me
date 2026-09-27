package ru.practicum.ewm.stats.repository;

public interface HitStatistics {
    String getApp();

    String getUri();

    Long getHits();
}
