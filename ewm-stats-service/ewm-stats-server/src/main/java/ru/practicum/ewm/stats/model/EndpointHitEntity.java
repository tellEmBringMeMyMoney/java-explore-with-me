package ru.practicum.ewm.stats.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "endpoint_hit")
public class EndpointHitEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 255)
    private String app;
    @Column(nullable = false, length = 2048)
    private String uri;
    @Column(nullable = false, length = 64)
    private String ip;
    @Column(nullable = false)
    private LocalDateTime timestamp;

    protected EndpointHitEntity() {
    }

    public EndpointHitEntity(String app, String uri, String ip, LocalDateTime timestamp) {
        this.app = app;
        this.uri = uri;
        this.ip = ip;
        this.timestamp = timestamp;
    }
}
