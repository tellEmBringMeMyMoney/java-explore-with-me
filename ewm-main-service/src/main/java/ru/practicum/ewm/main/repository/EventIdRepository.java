package ru.practicum.ewm.main.repository;

import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.model.EventEntity;

import java.util.List;

public interface EventIdRepository {
    List<Long> findIds(Specification<EventEntity> specification);
}
