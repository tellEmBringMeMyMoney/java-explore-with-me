package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.CommentEntity;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    @EntityGraph(attributePaths = "author")
    Page<CommentEntity> findByEventIdOrderByCreatedDescIdDesc(Long eventId, Pageable pageable);
}
