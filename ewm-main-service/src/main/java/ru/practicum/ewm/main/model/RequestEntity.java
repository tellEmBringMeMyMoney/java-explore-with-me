package ru.practicum.ewm.main.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests", uniqueConstraints = @UniqueConstraint(name = "uq_request_event_user", columnNames = {"event_id", "requester_id"}))
public class RequestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime created;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id")
    private EventEntity event;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    private UserEntity requester;
    @Column(nullable = false, length = 16)
    private String status;

    protected RequestEntity() {
    }

    public RequestEntity(LocalDateTime created, EventEntity event, UserEntity requester, String status) {
        this.created = created;
        this.event = event;
        this.requester = requester;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public EventEntity getEvent() {
        return event;
    }

    public UserEntity getRequester() {
        return requester;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String s) {
        status = s;
    }
}
