package ru.practicum.ewm.main.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class CommentEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000)
    private String text;
    @Column(nullable = false)
    private LocalDateTime created;
    @Column(nullable = false)
    private LocalDateTime updated;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private UserEntity author;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private EventEntity event;

    protected CommentEntity() { }
    public CommentEntity(String text, LocalDateTime now, UserEntity author, EventEntity event) {
        this.text = text; this.created = now; this.updated = now; this.author = author; this.event = event;
    }
    public Long getId() { return id; }
    public String getText() { return text; }
    public LocalDateTime getCreated() { return created; }
    public LocalDateTime getUpdated() { return updated; }
    public UserEntity getAuthor() { return author; }
    public EventEntity getEvent() { return event; }
    public void updateText(String value, LocalDateTime now) { text = value; updated = now; }
}
