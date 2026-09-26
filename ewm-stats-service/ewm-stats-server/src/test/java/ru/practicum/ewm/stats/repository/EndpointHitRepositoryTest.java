package ru.practicum.ewm.stats.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.practicum.ewm.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop", "spring.flyway.enabled=false"})
class EndpointHitRepositoryTest {
    @Autowired
    private EndpointHitRepository repository;

    @Test
    void savesAndAggregatesHitsForDatesUrisAndUniqueIps() {
        LocalDateTime start = LocalDateTime.parse("2025-01-01T00:00:00");
        LocalDateTime end = LocalDateTime.parse("2025-01-02T00:00:00");
        repository.saveAll(List.of(
                new EndpointHitEntity("main", "/events", "10.0.0.1", start),
                new EndpointHitEntity("main", "/events", "10.0.0.1", start.plusHours(1)),
                new EndpointHitEntity("main", "/events", "10.0.0.2", end),
                new EndpointHitEntity("main", "/other", "10.0.0.3", start.minusSeconds(1))));

        List<HitStatistics> all = repository.getStats(start, end, false);
        List<HitStatistics> unique = repository.getStatsByUris(start, end, List.of("/events"), true);

        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getHits()).isEqualTo(3L);
        assertThat(unique).hasSize(1);
        assertThat(unique.getFirst().getHits()).isEqualTo(2L);
    }
}
