package ru.practicum.ewm.main.model;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "compilations")
public class CompilationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String title;
    @Column(nullable = false)
    private boolean pinned;
    @org.hibernate.annotations.BatchSize(size = 32)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"), inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<EventEntity> events = new LinkedHashSet<>();

    protected CompilationEntity() {
    }

    public CompilationEntity(String title, boolean pinned) {
        this.title = title;
        this.pinned = pinned;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Set<EventEntity> getEvents() {
        return events;
    }

    public void setTitle(String t) {
        title = t;
    }

    public void setPinned(boolean p) {
        pinned = p;
    }

    public void setEvents(Set<EventEntity> e) {
        events = e;
    }
}
