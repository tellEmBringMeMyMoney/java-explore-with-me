package ru.practicum.ewm.main.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_reactions", uniqueConstraints = @UniqueConstraint(name = "uq_comment_reaction_user", columnNames = {"comment_id", "user_id"}))
public class CommentReactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comment_id", nullable = false)
    private CommentEntity comment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false, length = 8)
    private ReactionType type;

    protected CommentReactionEntity() {
    }

    public CommentReactionEntity(CommentEntity comment, UserEntity user, ReactionType type) {
        this.comment = comment;
        this.user = user;
        this.type = type;
    }

    public ReactionType getType() {
        return type;
    }

    public void setType(ReactionType value) {
        type = value;
    }
}
