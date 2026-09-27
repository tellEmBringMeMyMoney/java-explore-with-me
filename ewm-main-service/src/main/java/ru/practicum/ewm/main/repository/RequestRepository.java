package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.RequestEntity;

import java.util.List;

public interface RequestRepository extends JpaRepository<RequestEntity, Long> {
    interface EventRequestCount {
        Long getEventId();

        Long getCount();
    }

    @Query("select r.event.id as eventId, count(r.id) as count from RequestEntity r where r.status='CONFIRMED' and r.event.id in :eventIds group by r.event.id")
    List<EventRequestCount> countConfirmedForEvents(@Param("eventIds") List<Long> eventIds);

    List<RequestEntity> findByRequesterIdOrderByCreated(Long id);

    List<RequestEntity> findByEventIdAndRequesterIdIn(Long eventId, List<Long> requesterIds);

    List<RequestEntity> findByEventIdOrderByCreated(Long id);

    long countByEventIdAndStatus(Long eventId, String status);

    boolean existsByEventIdAndRequesterId(Long eventId, Long requesterId);

    @Query("select r from RequestEntity r join fetch r.requester where r.event.id=:event")
    List<RequestEntity> findDetailsByEvent(@Param("event") Long eventId);
}
