package ru.practicum.ewm.main.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@org.hibernate.annotations.BatchSize(size = 32)
@Entity
@Table(name = "events")
public class EventEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 2000)
    private String annotation;
    @Column(length = 7000)
    private String description;
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn;
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
    @Column(nullable = false)
    private double lat;
    @Column(nullable = false)
    private double lon;
    @Column(nullable = false)
    private boolean paid;
    @Column(name = "participant_limit", nullable = false)
    private int participantLimit;
    @Column(name = "request_moderation", nullable = false)
    private boolean requestModeration;
    @Column(nullable = false, length = 120)
    private String title;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiator_id")
    private UserEntity initiator;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private EventState state;

    protected EventEntity() {
    }

    public EventEntity(String annotation, String description, LocalDateTime eventDate, LocalDateTime createdOn, double lat, double lon, boolean paid, int limit, boolean moderation, String title, CategoryEntity category, UserEntity initiator) {
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.createdOn = createdOn;
        this.lat = lat;
        this.lon = lon;
        this.paid = paid;
        this.participantLimit = limit;
        this.requestModeration = moderation;
        this.title = title;
        this.category = category;
        this.initiator = initiator;
        this.state = EventState.PENDING;
    }

    public Long getId() {
        return id;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public LocalDateTime getPublishedOn() {
        return publishedOn;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public boolean isPaid() {
        return paid;
    }

    public int getParticipantLimit() {
        return participantLimit;
    }

    public boolean isRequestModeration() {
        return requestModeration;
    }

    public String getTitle() {
        return title;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public UserEntity getInitiator() {
        return initiator;
    }

    public EventState getState() {
        return state;
    }

    public void setState(EventState s) {
        state = s;
    }

    public void setPublishedOn(LocalDateTime t) {
        publishedOn = t;
    }

    public void setAnnotation(String v) {
        annotation = v;
    }

    public void setDescription(String v) {
        description = v;
    }

    public void setEventDate(LocalDateTime v) {
        eventDate = v;
    }

    public void setLat(double v) {
        lat = v;
    }

    public void setLon(double v) {
        lon = v;
    }

    public void setPaid(boolean v) {
        paid = v;
    }

    public void setParticipantLimit(int v) {
        participantLimit = v;
    }

    public void setRequestModeration(boolean v) {
        requestModeration = v;
    }

    public void setTitle(String v) {
        title = v;
    }

    public void setCategory(CategoryEntity v) {
        category = v;
    }
}
