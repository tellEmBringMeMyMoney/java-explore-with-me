package ru.practicum.ewm.main.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.main.model.CompilationEntity;

public interface CompilationRepository extends JpaRepository<CompilationEntity, Long> {
    Page<CompilationEntity> findByPinned(boolean pinned, Pageable pageable);
}
