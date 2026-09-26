package ru.practicum.ewm.stats.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHit;
import ru.practicum.ewm.stats.dto.ViewStats;
import ru.practicum.ewm.stats.model.EndpointHitEntity;
import ru.practicum.ewm.stats.repository.EndpointHitRepository;
import ru.practicum.ewm.stats.repository.HitStatistics;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class StatsService {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final EndpointHitRepository repository;

    public StatsService(EndpointHitRepository repository) {
        this.repository = repository;
    }

    public void save(EndpointHit hit) {
        repository.save(new EndpointHitEntity(hit.app(), hit.uri(), hit.ip(), LocalDateTime.parse(hit.timestamp(), FORMAT)));
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) throw new IllegalArgumentException("start must not be after end");
        List<HitStatistics> rows = uris == null || uris.isEmpty()
                ? repository.getStats(start, end, unique) : repository.getStatsByUris(start, end, uris, unique);
        return rows.stream().map(row -> new ViewStats(row.getApp(), row.getUri(), row.getHits())).toList();
    }
}
