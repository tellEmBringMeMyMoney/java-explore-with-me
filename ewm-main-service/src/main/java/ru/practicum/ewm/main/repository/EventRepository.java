package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.main.model.EventEntity;

import java.util.List;

public interface EventRepository extends JpaRepository<EventEntity, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<EventEntity>, EventIdRepository {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select e from EventEntity e join fetch e.category join fetch e.initiator where e.id=:id")
    java.util.Optional<EventEntity> findLockedDetailed(@Param("id") Long id);

    @Query("select e from EventEntity e join fetch e.category join fetch e.initiator where e.id=:id")
    java.util.Optional<EventEntity> findDetailed(@Param("id") Long id);

    @Query("select e from EventEntity e join fetch e.category join fetch e.initiator where e.initiator.id=:user")
    List<EventEntity> findByInitiator(@Param("user") Long user, Pageable pageable);

    long countByCategoryId(Long categoryId);
}
