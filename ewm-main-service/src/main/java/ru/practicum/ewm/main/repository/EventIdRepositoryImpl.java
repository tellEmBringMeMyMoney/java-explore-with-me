package ru.practicum.ewm.main.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.main.model.EventEntity;

import java.util.List;

public class EventIdRepositoryImpl implements EventIdRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Long> findIds(Specification<EventEntity> specification) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<EventEntity> event = query.from(EventEntity.class);
        query.select(event.get("id"))
                .where(specification.toPredicate(event, query, cb))
                .orderBy(cb.asc(event.get("eventDate")), cb.asc(event.get("id")));
        return entityManager.createQuery(query).getResultList();
    }
}
