package ru.practicum.ewm.stats.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHitEntity, Long> {
    @Query("select h.app as app, h.uri as uri, case when :unique = true then count(distinct h.ip) else count(h.id) end as hits "
            + "from EndpointHitEntity h where h.timestamp between :start and :end "
            + "group by h.app, h.uri order by hits desc, h.app, h.uri")
    List<HitStatistics> getStats(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                 @Param("unique") boolean unique);

    @Query("select h.app as app, h.uri as uri, case when :unique = true then count(distinct h.ip) else count(h.id) end as hits "
            + "from EndpointHitEntity h where h.timestamp between :start and :end and h.uri in :uris "
            + "group by h.app, h.uri order by hits desc, h.app, h.uri")
    List<HitStatistics> getStatsByUris(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
                                       @Param("uris") Collection<String> uris, @Param("unique") boolean unique);
}
